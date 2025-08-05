plugins {
    // Aplica o plugin de aplicação Java
    id("java")
    id("application")
    // Plugin essencial para criar um "fat JAR" que inclui todas as dependências
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.backpech.discordbot"
version = "1.0.0"

repositories {
    // Repositório padrão para buscar as dependências
    mavenCentral()
}

dependencies {
    // --- Dependências Principais do Bot ---

    // JDA (Java Discord API) - O coração do bot
    implementation("net.dv8tion:JDA:5.0.0-beta.22")

    // Owner - Para carregar o config.properties de forma segura
    implementation("org.aeonbits.owner:owner:1.0.12")

    // Logback - A implementação para o framework de logging SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.14")
    // SLF4J API - A interface de logging usada no código
    implementation("org.slf4j:slf4j-api:2.0.9")

    // SQLite JDBC - O driver de banco de dados para o GuildConfigService
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")

    // --- Dependências de Teste ---
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.1")
}

application {
    // Define a classe principal correta para a nossa aplicação
    mainClass.set("com.backpech.discordbot.Bot")
}

tasks.withType<Test> {
    // Configura o JUnit Platform para os testes unitários
    useJUnitPlatform()
}

// Configuração do plugin ShadowJar
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    archiveBaseName.set("discord-bot")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    mergeServiceFiles() // Importante para evitar conflitos de dependências
}