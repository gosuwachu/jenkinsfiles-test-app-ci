library identifier: 'jenkinsfiles-test-app-ci@main', retriever: modernSCM([
    $class: 'GitSCMSource',
    remote: 'https://github.com/gosuwachu/jenkinsfiles-test-app-ci.git',
    credentialsId: 'github-pat'
])

pipeline {
    agent any
    stages {
        stage('Checkout App') {
            steps { checkoutApp(env.COMMIT_SHA) }
        }
        stage('iOS Linter') {
            steps {
                script {
                    githubStatus.wrap(env.COMMIT_SHA, 'ci/ios-linter') {
                        echo 'Running iOS linter...'
                    }
                }
            }
        }
    }
}
