package tetrisDeluxe;

import java.awt.Color;

public class Shape 
{
	protected Color[] colors = 
		{
			new Color(0,255,0),
			new Color(255,0,0),
			new Color(0,255,255),
			new Color(255,255,0),
			new Color(255,165,0),
			new Color(0,0,255),
			new Color(128,0,128)
		};
	protected int[][][] shapes = 
		{
		    {{0, 1, 0, 0},  // Straight Line Shape 0
			 {0, 1, 0, 0},
			 {0, 1, 0, 0},
			 {0, 1, 0, 0}},

		    {{0, 0, 2, 0},  // Z-Shape
			 {0, 2, 2, 0},
			 {0, 2, 0, 0},
			 {0, 0, 0, 0}},

		    {{0, 3, 0, 0},  // Second Z-Shape 4
			 {0, 3, 3, 0},
			 {0, 0, 3, 0},
			 {0, 0, 0, 0}},

		    {{0, 0, 4, 0},  //Horizontally Fliped L-Shape 6
			 {0, 0, 4, 0},
			 {0, 4, 4, 0},
			 {0, 0, 0, 0}},

		    {{0, 5, 0, 0},  //L-Shape 10
			 {0, 5, 0, 0},
			 {0, 5, 5, 0},
			 {0, 0, 0, 0}},

		    {{0, 6, 0, 0},  //Cut F-shape 14
			 {0, 6, 6, 0},
			 {0, 6, 0, 0},
			 {0, 0, 0, 0}},

		    {{0, 0, 0, 0},  //Box shape 18
			 {0, 7, 7, 0},
			 {0, 7, 7, 0},
			 {0, 0, 0, 0}},
		};
}

