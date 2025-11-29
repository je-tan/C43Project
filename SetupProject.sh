mvn archetype:generate -DgroupId=cs.toronto.edu -DartifactId=pgsample -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cp Main.xml pgsample/pom.xml
cp Main.java pgsample/src/main/java/cs/toronto/edu/
cp User.java pgsample/src/main/java/cs/toronto/edu/
cp Portfolio.java pgsample/src/main/java/cs/toronto/edu/
cp Stocklist.java pgsample/src/main/java/cs/toronto/edu/
cp Friend.java pgsample/src/main/java/cs/toronto/edu/
cp Review.java pgsample/src/main/java/cs/toronto/edu/
cp Stock.java pgsample/src/main/java/cs/toronto/edu/
rm pgsample/src/main/java/cs/toronto/edu/App.java

