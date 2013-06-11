rm wardrobe/*.class
javac -d wardrobe java-impl/*.java
java -jar jasmin-2.4/jasmin.jar -d wardrobe jasm-impl/Console.jasm
