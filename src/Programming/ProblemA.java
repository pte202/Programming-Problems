package Programming;

import java.util.Arrays;
import java.util.Scanner;

public class ProblemA {
	
	
	public static void main(String args[]){
		
		int numberOfAirports;
		int numberOfRoutes;
		
		final int [] loungesValues = new int[] {0,1,2};
		
		Scanner userInput = new Scanner(System.in);
		
		numberOfAirports = userInput.nextInt();
		
		numberOfRoutes = userInput.nextInt();
		
		int [][] matrix = new int [numberOfRoutes][numberOfAirports];
		
		for(int route=0; route < numberOfRoutes; route++){
			
			
			int first;
			int second;
			int lounges;
			
			first = userInput.nextInt();
			second = userInput.nextInt();
			lounges = userInput.nextInt();
			
			System.out.println(lounges);
			
			if (lounges > 2 || lounges < 0){
				
				
				System.out.println("impossible");
				
			}
			
			if (lounges == 0){
				
				
			}
			
			
			
		}
		
		
		
		
		
	}

}
