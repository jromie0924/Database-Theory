// Jackson Romie
// Assignment 4

// FDS.java CS6051/EECE6010 2017 Cheng
// Algorithm 3.7 closure of X under F
// Usage: java FDS F X
// F is a file that has the first line all the attributes and
// then an FD a line with a space between the left-hand side and the right-hand side
// X is a string of characters represent a set of attributes

import java.io.*;
import java.util.*;

class FD{
  HashSet<Character> lhs; char rhs;
  public FD(HashSet<Character> l, char r){ lhs = l; rhs = r; } // lhs is a set of attributes; rhs is a single attribute.
  public boolean equals(Object obj){
    FD fd2 = (FD)obj;
    return lhs.equals(fd2.lhs) && rhs == fd2.rhs;
  }
};

public class FDS{
  HashSet<Character> R = new HashSet<Character>(); // all attributes
  HashSet<FD> F = new HashSet<FD>(); // the set of FDs

  public FDS(String filename){  // 1. split FDs so each FD has a single attribute on the right
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

  boolean follows(FD fd){  // fd follows from FDS
    boolean follows = false;
    HashSet<Character> closure = this.closure(fd.lhs);
    if (closure.contains(fd.rhs)) {
      follows = true;
    }
    return follows;
  }

  boolean equivalent(FDS T){
    for (FD f : T.F) {
      if(!this.follows(f)) {
        return false;
      }
    }

    for(FD f : this.F) {
      if(!T.follows(f)) {
        return false;
      }
    }

    return true;
  }

  public static String set2string(HashSet<Character> set) {
    String str = "";
    for(Character character : set) {
      str += character.toString();
    }
    return str;
  }

  public static List<String> getCombinations(String str, List<String> list) {
    if (list.contains(str))
      return list;

    list.add(str);

    for (int idx = 0; idx < str.length(); idx++) {
      StringBuilder nextStr = new StringBuilder(str);
      nextStr.deleteCharAt(idx);
      getCombinations(nextStr.toString(),list);
    }

    return list;
  }

  /*
  HashSet<Character> findAKey() {
    HashSet<Character> k = R;
    for(char c : R) {
      if (!closure(k.remove(c)).containsAll(R)) {
        k.add(c);
      }
    }
    return k;
  }

  */

  HashSet<Character> findAKey(){ // returns a key to the relation
    List<String> combinations = new Vector<String>();
    combinations = getCombinations(set2string(R), combinations);
    Collections.sort(combinations, (a, b) -> b.length() > a.length() ? 1 : -1);
    Collections.reverse(combinations);

    for (String combination: combinations) {
      boolean isKey = true;
      HashSet<Character> possibleKey = this.string2set(combination);
      if (this.closure(possibleKey).containsAll(this.R)) {
        for (char attr: possibleKey) {
          HashSet<Character> tmp = new HashSet<Character>(possibleKey);
          tmp.remove(attr);
          if (this.closure(tmp).containsAll(this.R)) {
            isKey = false;
          }
        }
      }
      else
        continue;

      if (isKey)
        return possibleKey;
    }

    return this.R;
  }

 public static void main(String[] args){
   String filename0 = "example3_8.txt";
   String filename1 = "Elmasri1424.txt";
   String filename2 = "Elmasri1425.txt";

   FDS fds0 = new FDS(filename0);
   FDS fds1 = new FDS(filename1);
   FDS fds2 = new FDS(filename2);

   for (FD f : fds0.F) {
     boolean follows = fds0.follows(f);
     if(follows) {
       System.out.print("Example 3.8 Functional Dependencies follows ");
       System.out.println(set2string(f.lhs) + " --> " + f.rhs);
     }
   }

   HashSet<Character> key1 = fds1.findAKey();
   HashSet<Character> key2 = fds2.findAKey();
   System.out.println("\nKey for Elmasri1424: " + set2string(key1));
   System.out.println("Key for Elmasri1425: " + set2string(key2));
 }
}
