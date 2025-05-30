def call(String repoDir = '.', String configPath = '', boolean failOnLeak = true) {
    dir(repoDir) {
        // Ensure Gitleaks is installed
        sh '''
            if ! command -v gitleaks &> /dev/null; then
                curl -sSL https://github.com/gitleaks/gitleaks/releases/latest/download/gitleaks-linux-amd64 -o gitleaks
                chmod +x gitleaks
                sudo mv gitleaks /usr/local/bin/gitleaks
            fi
        '''

        def reportJson = 'gitleaks-results.json'
        def reportHtml = 'gitleaks-report.html'

        def gitleaksCmd = "gitleaks detect --source=${repoDir} --report-format=json --report-path=${reportJson}"
        if (configPath?.trim()) {
            gitleaksCmd += " --config=${configPath}"
        }

        def gitleaksOutput = sh(script: "${gitleaksCmd} || true", returnStdout: true).trim()

        // Save raw output (if any) and convert to HTML
        def gitleaksResultText = readFile(file: reportJson)
        def gitleaksResultJson = readJSON text: gitleaksResultText

        writeFile file: reportJson, text: gitleaksResultText

        if (gitleaksResultJson) {
            echo "Gitleaks found secrets:"
            gitleaksResultJson.each { finding ->
                echo "- ${finding.RuleID} in ${finding.File} at line ${finding.StartLine}"
            }

            currentBuild.result = 'UNSTABLE'
            echo "Build marked as UNSTABLE due to secrets found."
        } else {
            echo "No secrets found in Gitleaks scan."
        }

        // Convert JSON to HTML
        convertGitleaksResultsToHtml(reportJson, reportHtml)

        // Archive the HTML report
        archiveArtifacts artifacts: reportHtml, allowEmptyArchive: true

        // Fail the build if configured to do so
        if (failOnLeak && gitleaksResultJson) {
            error "Gitleaks found secrets. Failing build."
        }
    }
}

// Helper method to convert JSON to HTML
def convertGitleaksResultsToHtml(String jsonFile, String htmlFile) {
    sh """
        echo '<html><body><h2>Gitleaks Scan Report</h2><table border="1">
        <tr><th>RuleID</th><th>File</th><th>Line</th><th>Secret</th></tr>' > ${htmlFile}
        cat ${jsonFile} | jq -c '.[]' | while read -r item; do
            rule=\$(echo \$item | jq -r .RuleID)
            file=\$(echo \$item | jq -r .File)
            line=\$(echo \$item | jq -r .StartLine)
            secret=\$(echo \$item | jq -r .Secret | sed 's/&/&amp;/g; s/</&lt;/g; s/>/&gt;/g')
            echo "<tr><td>\$rule</td><td>\$file</td><td>\$line</td><td><code>\$secret</code></td></tr>" >> ${htmlFile}
        done
        echo '</table></body></html>' >> ${htmlFile}
    """
}
