package rs.ac.bg.etf.pp1;

import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;



public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;
	
	Stack<List<Integer>> stackOfLists = new Stack<>();
	Stack<List<Integer>> stackOfBreakLists = new Stack<>();
	Stack<List<Integer>> stackOfContinueLists = new Stack<>();
	
	int patchAdr;
	int patchAdr1;
	int patchAdr2;
	int patchAdr3;
	
	List<Integer> lista = new ArrayList<>();
	
	
	int inFor;
	
	//for
	
	int incdec;
	int forbody;
	
	
	public CodeGenerator() {
		
	 Tab.chrObj.setAdr(Code.pc);
	 Code.put(Code.enter);
	 Code.put(Tab.chrObj.getLevel());
	 Code.put(Tab.chrObj.getLocalSymbols().size());
	 Code.put(Code.load_n);
	 Code.put(Code.exit);
	 Code.put(Code.return_);
	 
	 Tab.ordObj.setAdr(Code.pc);
	 Code.put(Code.enter);
	 Code.put(Tab.ordObj.getLevel());
	 Code.put(Tab.ordObj.getLocalSymbols().size());
	 Code.put(Code.load_n);
	 Code.put(Code.exit);
	 Code.put(Code.return_);
	 
	 Tab.lenObj.setAdr(Code.pc);
	 Code.put(Code.enter);
	 Code.put(Tab.lenObj.getLevel());
	 Code.put(Tab.lenObj.getLocalSymbols().size());
	 Code.put(Code.load_n);
	 Code.put(Code.arraylength);
	 Code.put(Code.exit);
	 Code.put(Code.return_);
	 
	 
		
	}
	
	public int getMainPc() {
		return mainPc;
	}
	
	
	
	public void visit(Read read) {
		
		
		if(read.getDesignator().obj.getType() == Tab.charType) {
					
					
					Code.put(Code.bread);
					Code.store(read.getDesignator().obj);
					
				}else {
					
					Code.put(Code.read);
					Code.store(read.getDesignator().obj);
				}
		
	}
	
	public void visit(Print print) {
		
		
		
		if(print.getExpr().struct == Tab.charType) {
			
			Code.loadConst(1);
			Code.put(Code.bprint);
			
		}else {
			Code.loadConst(5);
			Code.put(Code.print);
		}
		
		
		
	}
	
	public void visit(FactorNumber numconst) {
		
		Obj con = Tab.insert(Obj.Con,"$",numconst.struct);
		con.setLevel(0);
		con.setAdr(numconst.getN1());
		
		Code.load(con);//stavlja na expr stek
	}
	
	public void visit(FactorChar charconst) {
			
			Obj con = Tab.insert(Obj.Con,"$",charconst.struct);
			con.setLevel(0);
			con.setAdr(charconst.getC1());
			
			Code.load(con);//stavlja na expr stek
		}
	public void visit(FactorBool boolconst) {
		
		Obj con = Tab.insert(Obj.Con,"$",boolconst.struct);
		con.setLevel(0);
		con.setAdr(boolconst.getB1()? 1:0);
		
		Code.load(con);//stavlja na expr stek
	}
	
	public void visit(MethodTypeName methodName) {
		
		methodName.obj.setAdr(Code.pc);
		
		//Generate entery
		Code.put(Code.enter);
			//report_info(Integer.toString(methodName.obj.getLocalSymbols().size())+methodName.getMethName(),null);
		Code.put(methodName.obj.getLevel());
		Code.put(methodName.obj.getLocalSymbols().size());
	}
	
	public void visit(MethodTypeVoid methodName) {
			
			if("main".equalsIgnoreCase(methodName.getMethName())) {
				mainPc = Code.pc;
			}
			methodName.obj.setAdr(Code.pc);
			
			//Generate entery
			Code.put(Code.enter);
			Code.put(methodName.obj.getLevel());
			Code.put(methodName.obj.getLocalSymbols().size());
		}
	
	public void visit(MethodDeclType methDecl ) {
		//skida sa steka ono sto je enter stavio
//		Code.put(Code.exit);
//		Code.put(Code.return_);
		
		Code.put(Code.trap);
		Code.put(1);
	}
	
	public void visit(MethodDeclVoid methDecl ) {
		
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(DesignatorStmt design) {
		
		Code.store(design.getDesignator().obj);
		
	}
	
	public void visit(DesignatorIdent design) {
		
		SyntaxNode parent = design.getParent();
		
		if(DesignatorStmt.class != parent.getClass() && FuncCall.class != parent.getClass() && FactorDesignatorFunc.class != parent.getClass()) {
				
				Code.load(design.obj);
			
			}
		
		
			
		
	}
	
	public void visit(DesignatorColon design) {
		
		SyntaxNode parent = design.getParent();
		
		if(DesignatorStmt.class != parent.getClass() && FuncCall.class != parent.getClass() && FactorDesignatorFunc.class != parent.getClass()) {
			Code.load(design.obj);
		}
		
	}
	
	public void visit(DesignArray design) {
		
	SyntaxNode parent = design.getParent();
		
		
		
		if(DesignatorStmt.class != parent.getClass() && FuncCall.class != parent.getClass() && FactorDesignatorFunc.class != parent.getClass()) {
			Code.load(design.obj);
		}
		
	}
	
	public void visit(FuncCall funcCall) {
		
		Obj funcObj = funcCall.getDesignator().obj;
		int offset = funcObj.getAdr() - Code.pc;
		
		Code.put(Code.call);
		Code.put2(offset);
		
		if(funcCall.getDesignator().obj.getType() != Tab.noType) {
			Code.put(Code.pop);
		}
		
		
	}
	
	public void visit(FactorDesignatorFunc funcCall) {
			
			Obj funcObj = funcCall.getDesignator().obj;
			int offset = funcObj.getAdr() - Code.pc;
			
			Code.put(Code.call);
			Code.put2(offset);
			
	}
	
	public void visit(ReturnExpr returnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(Return returnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	public void visit(TermOpList term) {
		
		if(term.getAddop() instanceof Plus) {
			Code.put(Code.add);
		}else {
			Code.put(Code.sub);
		}
		
	}
	
	public void visit(MinTermExpr term) {
		Code.put(Code.neg);
	}
	
	public void visit(TermMulop term) {
		
		if(term.getMulop() instanceof Mul) {
			Code.put(Code.mul);
		}else if(term.getMulop() instanceof Div) {
			Code.put(Code.div);
		}else {
			Code.put(Code.rem);
		}
		
	}
	
	public void visit(FactorNew factor) {
		
		if(factor.getType().struct == Tab.charType) {
			Code.put(Code.newarray);
			Code.put(0);
		}else {
			Code.put(Code.newarray);
			Code.put(1);
		}
		
	}
	
	public void visit(DesignatorInc inc) {
		
		Code.put(Code.const_1);
		Code.put(Code.add);
		Code.store(inc.getDesignator().obj);
	}
	
	public void visit(DesignatorDec dec) {
			
			Code.put(Code.const_1);
			Code.put(Code.sub);
			Code.store(dec.getDesignator().obj);
		}
	
	public void visit(CondFactRelop cond) {
		
		if(inFor>0) {
			if(cond.getRelop().getClass() == Equal.class) {
				
				Code.putFalseJump(Code.eq,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}else if(cond.getRelop().getClass() == NotEqual.class) {
				
				Code.putFalseJump(Code.ne,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}else if(cond.getRelop().getClass() == Gth.class) {
				
				Code.putFalseJump(Code.gt,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}else if(cond.getRelop().getClass() == Geth.class) {
				
				Code.putFalseJump(Code.ge,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}else if(cond.getRelop().getClass() == Lth.class) {
				
				Code.putFalseJump(Code.lt,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}else {
				
				Code.putFalseJump(Code.le,0);
				patchAdr = Code.pc - 2;
				stackOfLists.peek().add(1, patchAdr);
				Code.putJump(0);
				patchAdr1 = Code.pc - 2;
				stackOfLists.peek().add(2, patchAdr1);
				
			}
		
		}
		
		
	}
	
	public void visit(NoConditionList cond) {
		
		Code.putJump(0);
		patchAdr = Code.pc - 2;
		stackOfLists.peek().add(1, patchAdr);
		Code.putJump(0);
		patchAdr1 = Code.pc - 2;
		stackOfLists.peek().add(2, patchAdr1);
		
		
	}
	
	
	public void visit(ForLoop loop) {
		
		lista = new ArrayList<>();
		
		stackOfLists.push(lista);
		stackOfBreakLists.push(new ArrayList<>());
		//stackOfContinueLists.push(new ArrayList<>());
		inFor++;
		
	}
	public void visit(ForStmt forloop) {
		
		
		//Code.putJump(patchAdr3);
		Code.putJump(stackOfLists.peek().get(3));
		
		//Code.fixup(patchAdr);
		
		Code.fixup(stackOfLists.peek().get(1));
		
		if(stackOfBreakLists.peek().size()>0) {
		
		Code.fixup(stackOfBreakLists.peek().get(0));
		}
		stackOfLists.pop();
		stackOfBreakLists.pop();
		//stackOfContinueLists.pop();
		inFor--;
	}

	
	public void visit(InFor infor) {
		//Code.fixup(patchAdr1);
		Code.fixup(stackOfLists.peek().get(2));
	}
	
	public void visit(Outincdec incdec) {
		//Code.putJump(patchAdr2);
		Code.putJump(stackOfLists.peek().get(0));
		
		
	}

	public void visit(ForCond forcond) {
		
		patchAdr2 = Code.pc;
		stackOfLists.peek().add(0,patchAdr2);
		
		
		
	}
	
	public void visit(Inincdec incdec) {
		
		
		patchAdr3 = Code.pc;
		stackOfLists.peek().add(3,patchAdr3);
		
		
		
	}
	
	public void visit(Break br) {
		
		Code.putJump(0);
		stackOfBreakLists.peek().add(Code.pc - 2);
		
	}
	
	public void visit(Continue con) {
		
		//stackOfContinueLists.peek().add(1);//da se zna da postoji continue
		
		Code.putJump(stackOfLists.peek().get(3));//skacem na inc/dec
		
		
		
	}
	
	
}
