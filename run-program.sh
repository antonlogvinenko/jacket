java -cp wardrobe:java-impl -jar jasmin-2.4/jasmin.jar -d wardrobe \
    wardrobe/$1.jasm wardrobe/Console.jasm
java -cp wardrobe:java-impl $1
