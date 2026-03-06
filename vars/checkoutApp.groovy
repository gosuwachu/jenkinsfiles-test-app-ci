def call(String commitSha) {
    checkout([
        $class: 'GitSCM',
        branches: [[name: commitSha]],
        userRemoteConfigs: [[
            url: 'https://github.com/gosuwachu/jenkinsfiles-test-app.git',
            credentialsId: 'github-pat'
        ]]
    ])
}
