# ════════════════════════════════════════════════════════════
#  Étage 1 : BUILD
#  Image Maven + JDK 21 (évite les soucis de permission/CRLF
#  du wrapper mvnw quand on build depuis Windows)
# ════════════════════════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# 1) On copie d'abord le pom seul et on télécharge les dépendances.
#    Tant que pom.xml ne change pas, cette couche est mise en cache
#    -> les rebuilds suivants sont beaucoup plus rapides.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2) Puis le code source, et on package (sans relancer les tests ici :
#    les tests tournent dans la CI GitHub Actions).
COPY src ./src
RUN mvn clean package -DskipTests -B

# ════════════════════════════════════════════════════════════
#  Étage 2 : RUNTIME
#  Image JRE seule (pas le JDK) -> image finale bien plus légère
# ════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Le plugin Spring Boot produit un seul *.jar exécutable
# (l'archive non-bootable est renommée en .jar.original, donc *.jar ne matche que le bon).
COPY --from=build /app/target/*.jar app.jar

# Documentaire uniquement (le vrai port vient de server.port) :
# auth 8081 | lawyer 8082 | booking 8083 | notification 8084 | audit 8085
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]