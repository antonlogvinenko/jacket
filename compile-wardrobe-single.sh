java -cp wardrobe:java-impl -jar jasmin-2.4/jasmin.jar -d wardrobe wardrobe/$1.jasm
cp wardrobe/$1.class target/classes/$1.class
