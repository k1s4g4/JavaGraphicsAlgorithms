# JavaGraphicsAlgorithms

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/painter.png)

  This project started as an intermediate level of developing my other project ConcreteView which uses OpenGL-ES but i had zero experience and knowledge about Computer Graphics so i started from the book "Computer Graphics for Java Programmers" by Leen Ammeraal. The book explains gradually every basic aspect of computer graphics such as coordinate systems and transformations from one to another, orthographic and perspective projection, data modelling and triangulation of polygons etc. It presents three algorithms for 3D representations of 3D objects which are :  
                              -Painter Algorithm
                              -zBuffer Algorithm
                              -Hidden Line Algorithm

I added some features such as controling the view with mouse instead of clicking the menu and painting the back buffer before showing it instead of repainting every time you change point of view. My final goal was to create a 3D model of steel that is contained in reinforced concrete beams. To achive that i took the torus tha you see in the picture above, rotate it and moved each quarter by some distance to achieve the result you see in the picture below.

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/steel.png)

Below are some photo of steel and torus object as shown by Hidden Line Algorithm:
Note: zBuffer shows the same result as Painter but in some cases zBuffer is better.

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/hiddenLines.png)

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/steelLines.png)
