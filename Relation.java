import java.io.*;
import java.util.*;
import java.util.function.*;

public class Relation{
	String name;
	int cols;
	int rows;
	String[] attributes;
	String[][] tuples;

  public Relation(){ }

  public Relation(String filename){
	Scanner in = null;
	try {
		in = new Scanner(new File(filename));
	} catch (FileNotFoundException e){
		System.err.println(filename + " not found.");
		System.exit(1);
	}
	String[] terms = in.nextLine().split("\t");
	name = terms[0];
	cols = Integer.parseInt(terms[1]);
	rows = Integer.parseInt(terms[2]);
	attributes = new String[cols];
	tuples = new String[rows][cols];
	terms = in.nextLine().split("\t");
	for (int c = 0; c < cols; c++) attributes[c] = terms[c];
	for (int r = 0; r < rows; r++){
		terms = in.nextLine().split("\t");
		for (int c = 0; c < cols; c++) tuples[r][c] = terms[c];
	}
	in.close();
  }

  public void showRelation(){
	System.out.println(name + "\t" + cols + "\t" + rows);
	System.out.print(attributes[0]);
	for (int c = 1; c < cols; c++) System.out.print("\t" + attributes[c]);
	System.out.println();
	for (int r = 0; r < rows; r++){
		System.out.print(tuples[r][0]);
		for (int c = 1; c < cols; c++) System.out.print("\t" + tuples[r][c]);
		System.out.println();
	}
  }

  public Relation project(String... attrs){
	int[] colIndex = new int[attrs.length];
	for (int c = 0; c < attrs.length; c++){
		int j = 0; for (; j < cols; j++) if (attrs[c].equals(attributes[j])) break;
		if (j == cols){
			System.err.println("attribute " + attrs[c] + " not found.");
			System.exit(1);
		}
		colIndex[c] = j;
	}
	Relation PR = new Relation();
	PR.cols = attrs.length;
	PR.name = "project(" + name;
	for (int c = 0; c < PR.cols; c++) PR.name += "," + attrs[c];
	PR.name += ")";
	PR.rows = rows;
	PR.attributes = new String[PR.cols];
	for (int c = 0; c < PR.cols; c++) PR.attributes[c] = attrs[c];
	PR.tuples = new String[rows][PR.cols];
	for (int r = 0; r < rows; r++)
		for (int c = 0; c < PR.cols; c++) PR.tuples[r][c] = tuples[r][colIndex[c]];
	return PR;
  }

  public Relation select(Function<Integer, Boolean> condition){
	Relation SR = new Relation();
	SR.cols= cols;
	SR.name = "select(" + name + ",condition)";
	int[] selected = new int[rows];
	int n = 0;
	for (int r = 0; r < rows; r++) if(condition.apply(r)) selected[n++] = r;
	SR.rows = n;
	SR.attributes = new String[cols];
	for (int c = 0; c < cols; c++) SR.attributes[c] = attributes[c];
	SR.tuples = new String[SR.rows][cols];
	for (int r = 0; r < SR.rows; r++)
		for (int c = 0; c < cols; c++) SR.tuples[r][c] = tuples[selected[r]][c];
	return SR;
  }

  public Relation join(Relation other, Function<int[], Boolean> condition){
	boolean[][] theta = new boolean[rows][other.rows];
	int[] pair = new int[2];
	int newRows = 0;
	for (int r1 = 0; r1 < rows; r1++){
		pair[0] = r1;
		for (int r2 = 0; r2 < other.rows; r2++){
			pair[1] = r2;
			theta[r1][r2] = condition.apply(pair);
			if (theta[r1][r2]) newRows++;
		}
	}
	Relation JR = new Relation();
	JR.name = name + "X" + other.name;
	JR.cols = cols + other.cols;
	JR.rows = newRows;
	JR.attributes = new String[JR.cols];
	for (int c = 0; c < cols; c++) JR.attributes[c] = attributes[c];
	for (int c = 0; c < other.cols; c++) JR.attributes[cols + c] = other.attributes[c];
	JR.tuples = new String[JR.rows][JR.cols];
	int r = 0;
	for (int r1 = 0; r1 < rows; r1++) for (int r2 = 0; r2 < other.rows; r2++)
		if (theta[r1][r2]){
			for (int c = 0; c < cols; c++) JR.tuples[r][c] = tuples[r1][c];
			for (int c = 0; c < other.cols; c++)
				JR.tuples[r][cols + c] = other.tuples[r2][c];
			r++;
		}
	return JR;
  }

  public static void main(String[] args){
	Relation Product = new Relation("files/Product.txt");
	Relation PC = new Relation("files/PC.txt");
	Relation Laptop = new Relation("files/Laptop.txt");

	System.out.println("Part A:\n");
	Relation RA = PC.select(r -> Double.parseDouble(PC.tuples[r][1]) >= 3.0);
	RA.join(Product, x -> RA.tuples[x[0]][0].equals(Product.tuples[x[1]][1])).showRelation();
	// TODO: Projection for part (A)

	System.out.println("---------------------------------------------------------------------\nPart B:\n");
	Relation RB = Laptop.select(r -> Double.parseDouble(Laptop.tuples[r][3]) >= 100);
	RB.join(Product, y -> RB.tuples[y[0]][0].equals(Product.tuples[y[1]][1])).showRelation();
	// TODO: Projection for part (B)
	
  }
}
