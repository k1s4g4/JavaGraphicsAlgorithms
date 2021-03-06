# JavaGraphicsAlgorithms

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/painter.png)

  This project started as an intermediate level of developing my other project ConcreteView which uses OpenGL-ES but i had no experience and knowledge about Computer Graphics so i started from the book "Computer Graphics for Java Programmers" by Leen Ammeraal. The book explains gradually every basic aspect of computer graphics such as coordinate systems and transformations from one to another, orthographic and perspective projection, data modelling, triangulation of polygons etc. It presents three algorithms for representing 3D objects which are :  
  
                              -Painter Algorithm
                              -zBuffer Algorithm
                              -Hidden Line Algorithm
                              
  Note: zBuffer in most cases shows the same result as Painter, but in some cases zBuffer is better. Painter Algorithm sorts all triangles by the average depth value of three vertices and start painting triangels from farther to nearer, so it paints nearer triangles over the farther. There is a problem with painter algorithm when you have intersecting triangles with similar depth values. For these occasions zBuffer takes care of this problem by having an array in which it holds for every pixel the minimum depth of the elements that were drew in it and every time it tries to draw something in this pixel checks if it is nearer, if yes it paints it and if no it proceeds to the next element.

  I added some features such as controling the view with mouse instead of clicking the menu and painting the back buffer before showing it instead of repainting every time you change point of view. My final goal was to create a 3D model of steel that is contained in reinforced concrete beams. To achieve that i took the torus that you see in the picture above, rotate it and moved each quarter by some distance to achieve the result you see in the picture below.

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/steel.png)


  


Below are some photos of steel and torus objects as shown by Hidden Line Algorithm:


![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/hiddenLines.png)

![alt text](https://github.com/k1s4g4/JavaGraphicsAlgorithms/blob/master/pics/steelLines.png)
