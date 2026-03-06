def set(String sha, String context, String state, String description) {
    withCredentials([usernamePassword(credentialsId: 'github-app',
            usernameVariable: 'GH_APP', passwordVariable: 'GH_TOKEN')]) {
        sh """curl -s -X POST \
            -H "Authorization: token \$GH_TOKEN" \
            -H "Accept: application/vnd.github+json" \
            "https://api.github.com/repos/gosuwachu/jenkinsfiles-test-app/statuses/${sha}" \
            -d '{"state":"${state}","context":"${context}","description":"${description}","target_url":"${env.BUILD_URL}"}'"""
    }
}

def wrap(String sha, String context, Closure body) {
    set(sha, context, 'pending', 'Running...')
    try {
        body()
        set(sha, context, 'success', 'Passed')
    } catch (e) {
        set(sha, context, 'failure', "Failed: ${e.message}")
        throw e
    }
}
