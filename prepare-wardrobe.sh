rm wardrobe/*
javac -d wardrobe java-impl/src/main/java/*.java
java -jar jasmin-2.4/jasmin.jar -d wardrobe jasm-impl/Console.jasm
