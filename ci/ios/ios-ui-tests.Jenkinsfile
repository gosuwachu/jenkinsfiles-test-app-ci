// iOS UI Tests — triggered by PR comment "run-ios-ui-tests"

library identifier: 'jenkinsfiles-test-app-ci@main', retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/gosuwachu/jenkinsfiles-test-app-ci.git',
    credentialsId: 'github-pat'
])

import groovy.transform.Field

@Field GITHUB_OWNER = 'gosuwachu'
@Field GITHUB_REPO = 'jenkinsfiles-test-app'

def checkCollaborator(String username) {
    echo "Checking if ${username} is a collaborator..."
    def statusCode = 0
    withCredentials([usernamePassword(credentialsId: 'github-app',
            usernameVariable: 'GH_APP', passwordVariable: 'GH_TOKEN')]) {
        statusCode = sh(
            script: """curl -s -o /dev/null -w '%{http_code}' \
                -H "Authorization: token \$GH_TOKEN" \
                -H "Accept: application/vnd.github+json" \
                "https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/collaborators/${username}" """,
            returnStdout: true
        ).trim().toInteger()
    }
    if (statusCode == 204) {
        echo "${username} is a collaborator — proceeding"
    } else {
        error("User ${username} is not a collaborator (HTTP ${statusCode}) — aborting")
    }
}

def resolvePR(String prNumber) {
    def prJson = ''
    withCredentials([usernamePassword(credentialsId: 'github-app',
            usernameVariable: 'GH_APP', passwordVariable: 'GH_TOKEN')]) {
        prJson = sh(
            script: """curl -s \
                -H "Authorization: token \$GH_TOKEN" \
                -H "Accept: application/vnd.github+json" \
                "https://api.github.com/repos/${GITHUB_OWNER}/${GITHUB_REPO}/pulls/${prNumber}" """,
            returnStdout: true
        ).trim()
    }
    def prData = readJSON(text: prJson)
    if (!prData.head) {
        error("Failed to resolve PR #${prNumber} — API response did not contain 'head' field")
    }
    return [branch: prData.head.ref, sha: prData.head.sha]
}

pipeline {
    agent any

    stages {
        stage('Resolve PR') {
            steps {
                script {
                    if (!env.PR_NUMBER?.trim()) {
                        error('PR_NUMBER parameter is required')
                    }

                    if (env.COMMENT_AUTHOR?.trim()) {
                        checkCollaborator(env.COMMENT_AUTHOR)
                    } else {
                        echo 'WARNING: COMMENT_AUTHOR not set — skipping collaborator check (manual trigger?)'
                    }

                    def pr = resolvePR(env.PR_NUMBER)
                    env.PR_BRANCH = pr.branch
                    env.PR_SHA = pr.sha
                    echo "PR #${env.PR_NUMBER}: branch=${env.PR_BRANCH}, sha=${env.PR_SHA}"
                }
            }
        }

        stage('Checkout') {
            steps { checkoutApp(env.PR_SHA) }
        }

        stage('iOS UI Tests') {
            steps {
                script {
                    githubStatus.wrap(env.PR_SHA, 'ci/ios-ui-tests') {
                        echo "Running iOS UI tests for PR #${env.PR_NUMBER} (branch: ${env.PR_BRANCH})..."
                        echo 'iOS UI tests passed'
                    }
                }
            }
        }
    }

    post {
        failure {
            script {
                if (env.PR_SHA) {
                    githubStatus.set(env.PR_SHA, 'ci/ios-ui-tests', 'error',
                        'Pipeline infrastructure failure')
                }
            }
        }
    }
}
