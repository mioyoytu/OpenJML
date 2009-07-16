package tests;

import org.jmlspecs.openjml.esc.JmlEsc;

import com.sun.tools.javac.util.Options;

/** This class of JUnit tests checks various uses of generic types.
 * @author David R. Cok
 *
 */
public class escgeneric extends EscBase {

    protected void setUp() throws Exception {
        //print = true;
        //noCollectDiagnostics = true;
        super.setUp();
        options.put("-noPurityCheck","");
        options.put("-nullableByDefault",""); // Because the tests were written this wasy
        //options.put("-jmlverbose",   "");
        //options.put("-jmldebug",   "");
        //options.put("-noInternalSpecs",   "");
        //JmlEsc.escdebug = true;
    }
    
    // FIXME - disabled until we get generic types implemented better
    public void _testConstructor() {
        options.put("-showbb","");
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava { \n"
                
                +"  public void m(Integer i) {\n"
                +"    Object oo = new TestG<Integer>(i);\n"
                +"  }\n"
                +"  public void ma(Object o) {\n"
                +"    Object oo = new TestG<Object>(o);\n"
                +"  }\n"
                +"}\n"
                +"class TestG<E> {\n"
                +"  //@   requires \\type(E) != \\type(Integer) ;\n"
                +"  public TestG(E i) {}\n"
                +"}"
                ,"/tt/TestJava.java:4: warning: The prover cannot establish an assertion (Precondition) in method m",17
                ,"/tt/TestJava.java:12: warning: Associated declaration",10
                );
    }
    
    /** Tests that we can reason about the result of \\typeof */
    public void testTypeOf() {
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava { \n"
                
                +"  public void m(Integer i) {\n"
                +"    //@ assert \\typeof(this) <: \\type(TestJava);\n"
                +"  }\n"
                +"  public void ma(Object o) {\n"
                +"    //@ assume \\typeof(this) == \\type(Object);\n"
                +"    //@ assert false;\n" // should not trigger
                +"  }\n"
                +"  public void mb(Object o) {\n"
                +"    //@ assert \\typeof(this) == \\type(Object);\n"
                +"  }\n"
                +"}\n"
                ,"/tt/TestJava.java:7: warning: An assumption appears to be infeasible in method ma(java.lang.Object)",9
                ,"/tt/TestJava.java:11: warning: The prover cannot establish an assertion (Assert) in method mb",9
                );
    }
    
    public void _testGenericType() {
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava<T extends B> { \n"
                
                +"  public void m(Integer i) {\n"
                +"    //@ assert \\type(TestJava<Integer>) != \\type(Object);\n"
                +"    //@ assert \\type(TestJava<Integer>) != \\type(TestJava<Object>);\n"
                +"  }\n"
                +"  public void ma(Object o) {\n"
                +"    //@ assert \\type(TestJava<Integer>) == \\type(TestJava<T>);\n"  // NO
                +"  }\n"
                +"  public void mb(Object o) {\n"
                +"    //@ assert \\typeof(this) == \\type(Object);\n"
                +"  }\n"
                +"}\n"
                +"class B {}\n"
                +"class C {}\n"
                ,"/tt/TestJava.java:7: warning: An assumption appears to be infeasible in method ma(java.lang.Object)",9
                ,"/tt/TestJava.java:11: warning: The prover cannot establish an assertion (Assert) in method mb",9
                );
    }
    
    public void testStatic() {
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava { \n"
                
                +"  public void m(Integer i) {\n"
                +"    TestG.<Integer>mm(i);\n"
                +"  }\n"
                +"  public void ma(Object o) {\n"
                +"    TestG.<Object>mm(o);\n"
                +"  }\n"
                +"}\n"
                +"class TestG {\n"
                +"  //@   requires \\type(E) != \\type(Integer) ;\n"
                +"  public static <E> void mm(E t) {}\n"
                +"}"
                ,"/tt/TestJava.java:4: warning: The prover cannot establish an assertion (Precondition) in method m",22
                ,"/tt/TestJava.java:11: warning: Associated declaration",18
                );
    }
    
    // TODO - invariants are not yet added to the overall class predicate
    public void _testInherit() {
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava { \n"
                
                +"  public void m(TestG<Integer> i, Integer j) {\n"
                +"    i.mm(j);\n"
                +"  }\n"
                +"  public void ma(TestG<Object> i, Object j) {\n"
                +"    i.mm(j);\n"
                +"  }\n"
                +"}\n"
                +"class TestG<E> {\n"
                +"    //@ requires \\type(E) != \\type(Integer);\n"
                +"    public void mm(E t) {}\n"
                +"}\n"
                ,"/tt/TestJava.java:4: warning: The prover cannot establish an assertion (Precondition) in method m",22
                ,"/tt/TestJava.java:11: warning: Associated declaration",18
                );
    }
    
    public void _testInherit2() {  // FIXME - disabled test
        helpTCX("tt.TestJava","package tt; \n"
                +"public class TestJava { \n"
                
                +"  public void m(TestG<Integer>.TestH i, Integer j) {\n"
                +"    i.mm(j);\n"
                +"  }\n"
                +"  public void ma(TestG<Object>.TestH i, Object j) {\n"
                +"    i.mm(j);\n"
                +"  }\n"
                +"}\n"
                +"class TestG<E> {\n"
                +"  class TestH extends TestG<Integer> {\n"
                +"    //@ requires \\type(E) != \\type(Integer);\n"
                +"    public void mm(E t) {}\n"
                +"  }\n"
                +"}\n"
                ,"/tt/TestJava.java:4: warning: The prover cannot establish an assertion (Precondition) in method m",22
                ,"/tt/TestJava.java:11: warning: Associated declaration",18
                );
    }
    
}
    