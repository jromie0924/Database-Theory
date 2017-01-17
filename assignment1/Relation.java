// Relation class and methods written by Dr. Yizong Cheng, University of Cincinnati
// Relation class main method written by Jackson Romie

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

	public Relation intersection(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = 0;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + " n " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}

	public Relation union(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = other.rows;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (!intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + " u " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		for (int r = 0; r < other.rows; r++)
			for (int c = 0; c < cols; c++) R.tuples[r][c] = other.tuples[r][c];
		int n = other.rows;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	 }

	public Relation difference(Relation other){
		if (cols != other.cols) return null;
		boolean[] intersects = new boolean[rows];
		int newRows = rows;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < other.rows; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(other.tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < other.rows;
			if (intersects[r]) newRows--;
		}
		Relation R = new Relation();
		R.name = name + " - " + other.name;
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}

	public Relation unique(){   // remove duplicate tuples
		boolean[] intersects = new boolean[rows];
		int newRows = 0;
		for (int r = 0; r < rows; r++){
			int r1 = 0; for (; r1 < r; r1++){
				int c = 0; for (; c < cols; c++)
					if (!tuples[r][c].equals(tuples[r1][c])) break;
				if (c == cols) break;
			}
			intersects[r] = r1 < r;
			if (!intersects[r]) newRows++;
		}
		Relation R = new Relation();
		R.name = name + ".uniq";
		R.cols = cols;
		R.rows = newRows;
		R.attributes = new String[cols];
		for (int c = 0; c < cols; c++) R.attributes[c] = attributes[c];
		R.tuples = new String[R.rows][cols];
		int n = 0;
		for (int r = 0; r < rows; r++) if (!intersects[r]){
			for (int c = 0; c < cols; c++) R.tuples[n][c] = tuples[r][c];
			n++;
		}
		return R;
	}


	public static void main(String[] args){
		Relation product = new Relation("files/Product.txt");
		Relation pc = new Relation("files/PC.txt");
		Relation laptop = new Relation("files/Laptop.txt");
		Relation printer = new Relation("files/Printer.txt");

		/*
		Task:
		What PC models have a speed of at least 3.00?
		*/
		System.out.println("Part A:\n");
		Relation pcOver3 = pc.select(r -> Double.parseDouble(pc.tuples[r][1]) >= 3.0);
		Relation threeOrGreater = pcOver3.join(product, x -> pcOver3.tuples[x[0]][0].equals(product.tuples[x[1]][1]));
		threeOrGreater = threeOrGreater.project("model");
		threeOrGreater.showRelation();
		// TODO: Projection for part (A)

		/*
		Task:
		Which manufacturers make laptops with a hard disk of at least 100GB?
		*/
		System.out.println("---------------------------------------------------------------------\nPart B:\n");
		Relation laptopOver3 = laptop.select(r -> Double.parseDouble(laptop.tuples[r][3]) >= 100);
		Relation makers = laptopOver3.join(product, x -> laptopOver3.tuples[x[0]][0].equals(product.tuples[x[1]][1]));
		makers = makers.project("maker");
		makers.showRelation();
		// TODO: Projection for part (B)

		/*
		Task:
		Find the model number and price of all products (of any time) made by manufacturer B.
		*/
		System.out.println("---------------------------------------------------------------------\nPart C:\n");
		Relation laptopProj = laptop.project("model", "price");
		Relation pcProj = pc.project("model", "price");
		Relation printerProj = printer.project("model", "price");
		Relation allProducts = laptopProj.union(pcProj).union(printerProj);
		Relation joined = allProducts.join(product, x -> allProducts.tuples[x[0]][0].equals(product.tuples[x[1]][1]));
		Relation makerB = joined.select(x -> joined.tuples[x][2].equals("B"));
		makerB = makerB.project("model", "price");
		makerB.showRelation();

		/*
		Task:
		Find the model numbers of all color laser printers.
		*/
		System.out.println("---------------------------------------------------------------------\nPart D:\n");
		Relation colorPrinters = printer.select(r -> printer.tuples[r][1].equals("true"));
		colorPrinters = colorPrinters.project("model");
		colorPrinters.showRelation();

		/*
		Task:
		Find the manufacturers that sell Laptops, but not PC's. (F, G)
		*/
		System.out.println("---------------------------------------------------------------------\nPart E:\n");
		Relation productProjE = product.project("maker", "type");
		Relation laptopMakers = productProjE.select(x -> productProjE.tuples[x][1].equals("laptop")).project("maker");
		laptopMakers = laptopMakers.unique();
		Relation pcMakers = productProjE.select(x -> productProjE.tuples[x][1].equals("pc")).project("maker");
		pcMakers = pcMakers.unique();
		Relation laptopOnlyMakers = laptopMakers.difference(pcMakers);
		laptopOnlyMakers.showRelation();

		/*
		// -----------------------------IN-CLASS QUIZ-----------------------------
		// Find the manufacturers of at least two different computers (PC's or laptops) with speeds of at least 2.80
		System.out.println("\n\n------------IN CLASS-------------\n\n");

		Relation R1 = pc.project("model", "speed").union(laptop.project("model", "speed"));
		Relation R2 = R1.select(x -> Double.parseDouble(R1.tuples[x][1]) >= 2.80).project("model");
		Relation R3 = R2.join(product, x -> R2.tuples[x[0]][0].equals(product.tuples[x[1]][1])).project("maker", "model");
		Relation R4 = R3.join(R3, x -> R3.tuples[x[0]][1].equals(R3.tuples[x[1]][1])).project("maker");
		R4.showRelation();
		*/
	}
}
