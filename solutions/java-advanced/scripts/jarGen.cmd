
javac -cp
 ..\..\..\shared\java-advanced-2023\artifacts\info.kgeorgiy.java.advanced.implementor.jar
..\java-solutions\info\kgeorgiy\ja\kadyrov\implementor\Implementor.java

jar -cfm ImplementorJar.jar MANIFEST.MF -C ..\java-solutions info\kgeorgiy\ja\kadyrov\implementor\Implementor.class
