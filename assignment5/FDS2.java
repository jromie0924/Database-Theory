// FDS2.java CS6051/EECE6010 2017 Cheng
// Algorithm 3.20 BCNF decomposition and Algorithm 3.12 Projection of FDs
// Usage: java FDS2 F
// F is a file that has the first line all the attributes and 
// then an FD a line with a space between the left-hand side and the right-hand side

import java.io.*;
import java.util.*;

class FD{
  HashSet<Character> lhs; char rhs;
  public FD(HashSet<Character> l, char r){ lhs = l; rhs = r; }
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs == fd2.rhs;
  }
  public void printout(){
    for (char c: lhs) System.out.print(c);
    System.out.print(" "); System.out.print(rhs); System.out.println();
  }
};

public class FDS2{
  HashSet<Character> R = new HashSet<Character>(); // all attributes
  HashSet<FD> F = new HashSet<FD>(); // the set of FDs

  public FDS2(String filename){  // 1. split FDs so each FD has a single attribute on the right
    Scanner in = null;
    try {
      in = new Scanner(new File(filename));
    } catch (FileNotFoundException e){
       System.err.println(filename + " not found");
       System.exit(1);
    }
    String line = in.nextLine();
    for (int i = 0; i < line.length(); i++) R.add(line.charAt(i));
    while (in.hasNextLine()){
      HashSet<Character> l = new HashSet<Character>();
      String[] terms = in.nextLine().split(" ");
      for (int i = 0; i < terms[0].length(); i++) l.add(terms[0].charAt(i));
      for (int i = 0; i < terms[1].length(); i++) F.add(new FD(l, terms[1].charAt(i)));
    }
    in.close();
  }

  public FDS2(HashSet<Character> r, HashSet<FD> f){ R = r; F = f; }

  HashSet<Character> string2set(String X){
    HashSet<Character> Y = new HashSet<Character>();
    for (int i = 0; i < X.length(); i++) Y.add(X.charAt(i));
    return Y;
  }

  void printSet(Set<Character> X){
    for (char c: X) System.out.print(c);
  }

  HashSet<Character> closure(HashSet<Character> X){ // Algorithm 3.7
    HashSet<Character> Xplus = new HashSet<Character>(X); // 2. initialize
    int len = 0;
    do { // 3. push out
      len = Xplus.size();
      for (FD fd: F)
        if (Xplus.containsAll(fd.lhs) && !Xplus.contains(fd.rhs)) Xplus.add(fd.rhs);
    } while (Xplus.size() > len);  
    return Xplus; // 4. found closure of X
  }

 FD BCNFviolation(){
	for (FD fd: F) if (!closure(fd.lhs).containsAll(R)) return fd;
	return null;
 }

 void BCNFdecompose(){  // Algorithm 3.20
	FD fd = BCNFviolation();
	if (fd == null){
	  printSet(R); System.out.println();
	  for (FD f: F){
		boolean redundant = false;
		for (FD f2: F) // remove those with lhs larger than necessary
			if (f2.rhs == f.rhs && f.lhs.containsAll(f2.lhs) && !f.lhs.equals(f2.lhs)){
				redundant = true; 
				break;
			}
		if (!redundant) f.printout();
	  }
	  System.out.println();
	}else{  
		HashSet<Character> R1 = closure(fd.lhs);
		HashSet<Character> R2 = new HashSet<Character>(fd.lhs);
		for (char c: R) if (!R1.contains(c)) R2.add(c);  // decomposed into R1 and R2
		FDS2 fds1 = new FDS2(R1, project(R1, R1, new HashSet<Character>()));
		fds1.BCNFdecompose();  // recursive call on R1
		FDS2 fds2 = new FDS2(R2, project(R2, R2, new HashSet<Character>()));
		fds2.BCNFdecompose();  // recursive call on R2
	}
  }
		
 HashSet<FD> project(HashSet<Character> R1, HashSet<Character> candidates, HashSet<Character> X){
	// Algorithm 3.12, starts with candidates = R1 and subset X = empty
	HashSet<FD> T = new HashSet<FD>(); // Let T be the eventual output set of FD's.
	// Initially, T is empty.  Step 1
	// This recursive function generates all subsets of R1 in a depth-first way by set inclusion
	HashSet<Character> Xplus = closure(X);  //  Step 2
	if (X.size() > 0)
	  for (char A: Xplus) if (A is not in X and A is in R1) T.add(new FD(X, A));
	if (!Xplus.containsAll(R)){  // otherwise no superset needs to be explored 
// If we already know that the closure of X is all attributes, then we cannot discover any
// new FD's by closing supersets of X. p.83
		HashSet<Character> candidates2 = new HashSet<Character>(candidates);
		for (char c: candidates){  // go through all superset of X by adding one attribute
			candidates2.remove(A);
			X.add(A);
			T.addAll(project(R1, new HashSet<Character>(candidates2), 
				new HashSet<Character>(X)));
			X.remove(A);
		}
	}
	return T;
  }

 public static void main(String[] args){
    FDS2 fds = new FDS2(args[0]);     
    fds.BCNFdecompose();
 }
}
