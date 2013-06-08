rm wardrobe/*.class
javac java-impl/*.java
java -jar jasmin-2.4/jasmin.jar -d wardrobe jasm-impl/Console.jasm
