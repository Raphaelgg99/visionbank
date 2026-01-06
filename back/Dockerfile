# ====== BUILD ======
FROM ubuntu:24.04 AS build

# Instalação dos pacotes necessários
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Baixa as dependências antes de copiar o código, para usar cache
COPY pom.xml .
RUN mvn -q -B -DskipTests dependency:go-offline

# Copia o código fonte
COPY src ./src
RUN mvn -q -B -DskipTests clean package

# ====== RUNTIME ======
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
EXPOSE 8080

# Copia o arquivo JAR gerado
COPY --from=build /app/target/*.jar app.jar

# Comando para rodar a aplicação
ENTRYPOINT ["java","-jar","app.jar"]


