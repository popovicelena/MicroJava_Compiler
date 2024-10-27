package rs.ac.bg.etf.pp1;

import java.util.*;
import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;

public class SemanticAnalyzer extends VisitorAdaptor {

	boolean errorDetected = false;
	int printCallCount = 0;
	int varDeclCount = 0;
	int nVars;
	Obj currentMethod = null;
	boolean returnFound = false;
	boolean inNamespace = false;
	String currNamespace = null;
	Struct currentType = null;
	int inFor = 0;

	List<Expr> actparametars = new ArrayList<>();

	Stack<List<Struct>> actpars = new Stack<>();// stek listi za stvarne parametre

	List<String> namespaces = new ArrayList<>();// cuvam listu imena namespaceova da ne bi imala dva namesapcea sa istim
												// imenom

	public static final Struct boolType = new Struct(Struct.Bool);

	public SemanticAnalyzer() {
		Tab.currentScope.addToLocals(new Obj(Obj.Type, "bool", boolType));
	}

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {

		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(VarIdent varDecl) {

		varDeclCount++;
		if (inNamespace && currentMethod == null) {
			Obj name = Tab.currentScope.findSymbol(currNamespace + "::" + varDecl.getVarName());
			if (name == null) {

				if (varDecl.getSquare() instanceof Squares) {

					report_info(" Deklarisan niz " + varDecl.getVarName(), varDecl);
					Obj varNode = Tab.insert(Obj.Var, currNamespace + "::" + varDecl.getVarName(),
							new Struct(Struct.Array, currentType));

				} else {
					report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
					Obj varNode = Tab.insert(Obj.Var, currNamespace + "::" + varDecl.getVarName(), currentType);

				}
			} else {
				report_error("Greska na liniji " + varDecl.getLine() + " : promenljiva sa imenom "
						+ varDecl.getVarName() + " je vec deklarisana! ", null);
			}
		} else {
			Obj var = Tab.currentScope.findSymbol(varDecl.getVarName());
			if (var == null) {
				if (varDecl.getSquare() instanceof Squares) {

					report_info("Deklarisan niz " + varDecl.getVarName(), varDecl);
					Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, currentType));

				} else {
					report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
					Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), currentType);

				}
			} else {
				report_error("Greska na liniji " + varDecl.getLine() + " : promenljiva sa imenom "
						+ varDecl.getVarName() + " je vec deklarisana! ", null);
			}
		}

	}

	public void visit(CnstInitialize constDecl) {

		if (inNamespace) {
			// namespace
			Obj name = Tab.currentScope.findSymbol(currNamespace + "::" + constDecl.getConstName());
			if (name == null) {
				if (currentType == constDecl.getValue().struct) {
					report_info("Deklarisana simbolicka konstanta " + constDecl.getConstName(), constDecl);
					Obj varNode = Tab.insert(Obj.Con, currNamespace + "::" + constDecl.getConstName(), currentType);

					if (constDecl.getValue() instanceof ValueNumber) {
						varNode.setAdr(((ValueNumber) constDecl.getValue()).getNum());
					} else if (constDecl.getValue() instanceof ValueChar) {
						varNode.setAdr(((ValueChar) constDecl.getValue()).getChr());
					} else {
						varNode.setAdr(((ValueBool) constDecl.getValue()).getBool() ? 1 : 0);
					}

				} else {
					report_error("Greska na liniji " + constDecl.getLine()
							+ " : ne poklapaju se deklarisan tip i tip dodeljene vrednosti! ", null);
				}
			} else {
				report_error("Greska na liniji " + constDecl.getLine() + " : konstanta sa imenom "
						+ constDecl.getConstName() + " je vec deklarisana! ", null);
			}
		} else {

			// van namespacea

			Obj name = Tab.currentScope.findSymbol(constDecl.getConstName());
			if (name == null) {
				if (currentType == constDecl.getValue().struct) {
					report_info("Deklarisana simbolicka konstanta " + constDecl.getConstName(), constDecl);
					Obj varNode = Tab.insert(Obj.Con, constDecl.getConstName(), currentType);

					if (constDecl.getValue() instanceof ValueNumber) {
						varNode.setAdr(((ValueNumber) constDecl.getValue()).getNum());
					} else if (constDecl.getValue() instanceof ValueChar) {
						varNode.setAdr(((ValueChar) constDecl.getValue()).getChr());
					} else {
						varNode.setAdr(((ValueBool) constDecl.getValue()).getBool() ? 1 : 0);
					}

				} else {
					report_error("Greska na liniji " + constDecl.getLine()
							+ " : ne poklapaju se deklarisan tip i tip dodeljene vrednosti! ", null);
				}
			} else {
				report_error("Greska na liniji " + constDecl.getLine() + " : konstanta sa imenom "
						+ constDecl.getConstName() + " je vec deklarisana! ", null);
			}

		}

	}

	public void visit(ValueNumber val) {
		val.struct = Tab.intType;
	}

	public void visit(ValueBool val) {
		val.struct = boolType;
	}

	public void visit(ValueChar val) {
		val.struct = Tab.charType;
	}

	public void visit(Print print) {

		Struct expr = print.getExpr().struct;

		if (!(expr == Tab.intType) && !(expr == Tab.charType) && !(expr == boolType)) {
			report_error("Greska na liniji: " + print.getLine()
					+ ": unutar funkcije print mogu se naci samo int, char ili bool tip!", null);
		}
		printCallCount++;

	}

	public void visit(ProgName progName) {

		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);

		Tab.openScope();

	}

	public void visit(Program program) {

		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(TypeIdent type) {
		Obj typeNode = Tab.find(type.getTypeName());

		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
			type.struct = Tab.noType;
		} else {

			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!" + typeNode.getKind(), type);
				type.struct = Tab.noType;
			}

		}
		currentType = type.struct;
	}

	public void visit(TypeColon type) {
		Obj typeNode = Tab.find(type.getTypeName());

		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola! ", null);
			type.struct = Tab.noType;
		} else {

			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!" + typeNode.getKind(), type);
				type.struct = Tab.noType;
			}

		}
		currentType = type.struct;
	}

	public void visit(MethodTypeName methodTypeName) {

		if (inNamespace) {

			Obj name = Tab.currentScope.findSymbol(currNamespace + "::" + methodTypeName.getMethName());
			if (name == null) {

				currentMethod = Tab.insert(Obj.Meth, currNamespace + "::" + methodTypeName.getMethName(),
						methodTypeName.getType().struct);
				methodTypeName.obj = currentMethod;
				Tab.openScope();
				report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
			} else {
				
				currentMethod = Tab.noObj;
				methodTypeName.obj = currentMethod;
				report_error("Greska na liniji " + methodTypeName.getLine() + " : funkcija sa imenom "
						+ methodTypeName.getMethName() + " je vec deklarisana! ", null);
				

			}

		} else {

			Obj method = Tab.currentScope.findSymbol(methodTypeName.getMethName());
			if (method == null) {

				if (methodTypeName.getMethName().equals("main")) {
					report_error("Greska na liniji " + methodTypeName.getLine() + " : funkcija "
							+ methodTypeName.getMethName() + " nema povratnu vrednost ", null);
					currentMethod = Tab.noObj;
					methodTypeName.obj = currentMethod;

					// return;
				}
				currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), methodTypeName.getType().struct);
				methodTypeName.obj = currentMethod;
				Tab.openScope();
				report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
			} else {
				
				currentMethod = Tab.noObj;
				methodTypeName.obj = currentMethod;
				report_error("Greska na liniji " + methodTypeName.getLine() + " : funkcija sa imenom "
						+ methodTypeName.getMethName() + " je vec deklarisana! ", null);
				
				// Tab.openScope();
			}
		}

	}

	public void visit(MethodTypeVoid methodTypeName) {

		if (inNamespace) {

			Obj name = Tab.currentScope.findSymbol(currNamespace + "::" + methodTypeName.getMethName());
			if (name == null) {

				currentMethod = Tab.insert(Obj.Meth, currNamespace + "::" + methodTypeName.getMethName(), Tab.noType);
				methodTypeName.obj = currentMethod;
				Tab.openScope();
				report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
			} else {
				
				currentMethod = Tab.noObj;
				methodTypeName.obj = currentMethod;
				report_error("Greska na liniji " + methodTypeName.getLine() + " : funkcija sa imenom "
						+ methodTypeName.getMethName() + " je vec deklarisana! ", null);
				
				// Tab.openScope();
			}

		} else {

			Obj method = Tab.currentScope.findSymbol(methodTypeName.getMethName());
			if (method == null) {

				currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), Tab.noType);
				methodTypeName.obj = currentMethod;
				Tab.openScope();
				report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
			} else {
				currentMethod = Tab.noObj;
				methodTypeName.obj = currentMethod;
				report_error(
						"Greska na liniji " + methodTypeName.getLine() + " : funkcija sa imenom "
								+ methodTypeName.getMethName() + " je vec deklarisana! ",
						null);
				
				

			}
		}
	}

	public void visit(MethodDeclType methodDecl) {

		if (currentMethod == Tab.noObj) {

			returnFound = false;
			currentMethod = null;
			return;
		}

		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funcija " + currentMethod.getName()
					+ " nema return iskaz!", null);

		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;

	}

	public void visit(MethodDeclVoid methodDecl) {

		if (currentMethod == Tab.noObj) {

			returnFound = false;
			currentMethod = null;
			return;
		}

		if (!returnFound && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funcija " + currentMethod.getName()
					+ " nema return iskaz!", null);
		}

		if (methodDecl.getMethodTypeVoid().getMethName().equals("main")
				&& methodDecl.getFormPars().getClass().getSimpleName().equals("FormalParametars")) {
			report_error("Greska na liniji " + methodDecl.getLine() + " : funkcija "
					+ methodDecl.getMethodTypeVoid().getMethName() + " nema argumente ", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;

	}

	public void visit(DesignatorIdent d) {

		if (inNamespace) {

			// namespace
			if (currentMethod == null) {

				Obj obj = Tab.find(currNamespace + "::" + d.getDesignName());
				if (obj == Tab.noObj) {
					//d.obj = obj;
					report_error("Greska na liniji " + d.getLine() + " : ime " + d.getDesignName()
							+ " nije deklarisano u prostoru imena " + currNamespace, null);
				}

				if (obj.getKind() == Obj.Meth) {

					actpars.push(new ArrayList<>());

				}
				d.obj = obj;

			} else {

				Obj obj = Tab.find(d.getDesignName());

				if (obj == Tab.noObj) {

					obj = Tab.find(currNamespace + "::" + d.getDesignName());
					if (obj == Tab.noObj) {
						report_error("Greska na liniji " + d.getLine() + " : ime " + d.getDesignName()
								+ " nije dekl arisano u prostoru imena " + currNamespace, null);

					}

					if (obj.getKind() == Obj.Meth) {

						actpars.push(new ArrayList<>());

					}
					d.obj = obj;

				}

				d.obj = obj;

			}

		} else {
			// ako nije namespace
			Obj obj = Tab.find(d.getDesignName());
			if (obj == Tab.noObj) {
				report_error("Greska na liniji " + d.getLine() + " : ime " + d.getDesignName() + " nije deklarisano! ",
						null);
			}

			if (obj.getKind() == Obj.Meth) {

				actpars.push(new ArrayList<>());

			}

			d.obj = obj;

		}

	}

	public void visit(DesignatorColon d) {

		Obj obj = Tab.find(d.getNamespaceName() + "::" + d.getDesignName());

		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + d.getLine() + " : ime " + d.getDesignName()
					+ " nije deklarisano u prostoru imena " + d.getNamespaceName(), null);
		}

		if (obj.getKind() == Obj.Meth) {

			actpars.push(new ArrayList<>());

		}

		d.obj = obj;
	}

	public void visit(FuncCall funcCall) {

		Obj func = funcCall.getDesignator().obj;

		if (Obj.Meth == func.getKind()) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		} else {
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!",
					null);
			funcCall.struct = Tab.noType;
		}
		// ovde

		Obj method = funcCall.getDesignator().obj;
		if(!actpars.isEmpty()) {
		if (method.getLevel() != actpars.peek().size()) {
			report_error("Greska na liniji " + funcCall.getLine() + " : broj stvarnih i formalnih parametara funkcije "
					+ method.getName() + " se razlikuje!", null);
		} else {

			Iterator<Obj> iterator = method.getLocalSymbols().iterator();

			for (int i = 0; i < actpars.peek().size(); i++) {
				if (!actpars.peek().get(i).assignableTo(iterator.next().getType())) {
					report_error("Greska na liniji " + funcCall.getLine()
							+ " : nisu kompatibilni tipovi formalnih i stvarnih parametara funkcije " + method.getName()
							+ "!", null);
					break;
				}
			}
		}

		// actparametars.clear();

		actpars.pop();
		
		}

	}

	public void visit(MinTermExpr term) {
		if (term.getTerm().struct != Tab.intType) {
			report_error("Greska na liniji " + term.getLine() + " : " + "iza minusa mora biti intType", null);
		} else {
			term.struct = Tab.intType;
		}
	}

	public void visit(TermExpr term) {
		term.struct = term.getTerm().struct;
	}

	public void visit(TermOpList addExpr) {
		Struct te = addExpr.getExpr().struct;
		Struct t = addExpr.getTerm().struct;
		if (te.equals(t) && te == Tab.intType)
			addExpr.struct = te;
		else {
			report_error("Greska na liniji " + addExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.",
					null);
			addExpr.struct = Tab.noType;
		}
	}

	public void visit(TermMulop addExpr) {
		Struct te = addExpr.getTerm().struct;
		Struct t = addExpr.getFactor().struct;
		if (te.equals(t) && te == Tab.intType)
			addExpr.struct = te;
		else {
			report_error("Greska na liniji " + addExpr.getLine() + " : nekompatibilni tipovi u izrazu za mnozenje.",
					null);
			addExpr.struct = Tab.noType;
		}
	}

	public void visit(TermFactor term) {
		term.struct = term.getFactor().struct;
	}

	public void visit(FactorNumber factor) {
		factor.struct = Tab.intType;
	}

	public void visit(FactorChar factor) {

		factor.struct = Tab.charType;
	}

	public void visit(FactorBool factor) {
		factor.struct = boolType;
	}

	public void visit(FactorNew factor) {
		factor.struct = new Struct(Struct.Array, factor.getType().struct);
	}

	public void visit(FactorDesignator factor) {
		Obj designator = factor.getDesignator().obj;
		factor.struct = designator.getType();

	}

	public void visit(FactorDesignatorFunc factor) {
		Obj design = factor.getDesignator().obj;
		factor.struct = design.getType();
		// actparametars.clear();

		Obj method = factor.getDesignator().obj;
		if(!actpars.isEmpty()) {
		if (method.getLevel() != actpars.peek().size()) {
			report_error("Greska na liniji " + factor.getLine() + " : broj stvarnih i formalnih parametara funkcije "
					+ ((DesignatorIdent) factor.getDesignator()).getDesignName() + " se razlikuje!", null);
		} else {

			Iterator<Obj> iterator = method.getLocalSymbols().iterator();

			for (int i = 0; i < actpars.peek().size(); i++) {
				if (!actpars.peek().get(i).assignableTo(iterator.next().getType())) {
					report_error("Greska na liniji " + factor.getLine()
							+ " : nisu kompatibilni tipovi formalnih i stvarnih parametara!", null);
					break;
				}
			}
		}
		actpars.pop();
		}
	}

	public void visit(DesignatorStmt design) {

		Obj designator = design.getDesignator().obj;
		Struct expr = design.getExpr().struct;

		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Greska na liniji " + design.getLine()
					+ " : sa leve strane jednakosti mora biti ili promenljiva ili element niza!", null);
		}

		if (!expr.assignableTo(designator.getType())) {
			report_error("Greska na liniji " + design.getLine()
					+ " : nisu kompatibilni tipovi sa leve i desne strane jednakosti!", null);
		}

	}

	public void visit(DesignArray d) {

		if (d.getExpr().struct != Tab.intType) {
			report_error("Greska na liniji " + d.getLine() + " : index niza mora biti tipa int!", null);
		}

		if (d.getDesignator() instanceof DesignatorIdent) {
			DesignatorIdent design = (DesignatorIdent) d.getDesignator();
			if (d.getDesignator().obj.getType().getKind() != Struct.Array) {
				report_error("Greska na liniji " + d.getLine() + " : moze se pristupati zamo nizu!", null);
			}
			d.obj = new Obj(Obj.Elem, ((DesignatorIdent) d.getDesignator()).getDesignName(),
					((DesignatorIdent) d.getDesignator()).obj.getType().getElemType());

		}

		if (d.getDesignator() instanceof DesignatorColon) {
			DesignatorColon design = (DesignatorColon) d.getDesignator();
			if (d.getDesignator().obj.getType().getKind() != Struct.Array) {
				report_error("Greska na liniji " + d.getLine() + " : moze se pristupati zamo nizu!", null);
			}
			d.obj = new Obj(Obj.Elem,
					((DesignatorColon) d.getDesignator()).getNamespaceName() + "::"
							+ ((DesignatorColon) d.getDesignator()).getDesignName(),
					((DesignatorColon) d.getDesignator()).obj.getType().getElemType());

		}

	}

	public void visit(DesignatorInc design) {
		Obj designator = design.getDesignator().obj;
		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Greska na liniji " + design.getLine()
					+ " : samo promenljiva ili element niza se mogu inkrementirati!", null);
		}
		if (designator.getType() != Tab.intType) {
			report_error("Greska na liniji " + design.getLine() + " : promenlijva mora biti celobrojnog tipa!", null);
		}
	}

	public void visit(DesignatorDec design) {
		Obj designator = design.getDesignator().obj;
		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Greska na liniji " + design.getLine()
					+ " : samo promenljiva i elemnt niza se mogu dekrementirati!", null);
		}
		if (designator.getType() != Tab.intType) {
			report_error("Greska na liniji " + design.getLine() + " : promenlijva mora biti celobrojnog tipa!", null);
		}
	}

	public void visit(DesignatorsDesignatorComma design) {

		Obj designator = design.getDesignator().obj;

		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error(
					"Greska na liniji " + design.getDesignator().getLine()
							+ " : sa leve strane jednakosti ispred zareza mora biti ili promenljiva ili element niza!",
					null);
		}
		if (designator.getKind() == Obj.Var) {
			if (designator.getType().getKind() == Struct.Array) {
				report_error("Greska na liniji " + design.getDesignator().getLine()
						+ " : sa leve strane jednakosti ispred zareza mora biti ili promenljiva ili element niza!",
						null);
			}
		}

	}

	public void visit(DesignatorMul design) {

		Obj array = design.getDesignator1().obj;
		Obj muldesign = design.getDesignator().obj;
		Struct expr = array.getType().getElemType();
		Struct exprMul = muldesign.getType().getElemType();
		
		if(muldesign == Tab.noObj) {
			report_error("Greska na liniji " + design.getLine()
					+ " ime: "+muldesign.getName()+ "nije deklarisano!", null);
			exprMul = Tab.noType;
		}

		if (array.getType().getKind() != Struct.Array) {
			report_error("Greska na liniji " + design.getDesignator1().getLine()
					+ " : sa desne strane jednakosti mora biti niz!", null);
		}
		if (muldesign.getType().getKind() != Struct.Array) {
			report_error("Greska na liniji " + design.getDesignator().getLine() + " : iza znaka * mora biti niz!",
					null);
		}

		if (design.getDesigncomma() instanceof DesignatorsDesignatorComma) {
			Obj comma = ((DesignatorsDesignatorComma) design.getDesigncomma()).getDesignator().obj;
			if (!expr.assignableTo(comma.getType())) {
				report_error("Greska na liniji " + design.getDesignator().getLine()
						+ " : tip elemenata niz sa desne strane znaka za dodelu mora biti kompatibilan sa tipom svih promenljivih sa leve strane znaka!",
						null);
			}
		}

		if (!exprMul.assignableTo(expr)) {
			report_error("Greska na liniji " + design.getDesignator().getLine()
					+ " : nekompatibilne vrednosti sa desne i leve strane znaga jednakosti!", null);
		}
	}

	public void visit(ReturnExpr returnExpr) {

		if (currentMethod == null) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : "
					+ "return ne sme postojati izvan tela metoda odnosno globalnih funkcija!", null);
		}

		returnFound = true;
		Struct currMethodType = currentMethod.getType();
		if (!currMethodType.compatibleWith(returnExpr.getExpr().struct)) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : "
					+ "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
					+ currentMethod.getName(), null);
		}

	}

	public void visit(Read readStmt) {

		Obj designator = readStmt.getDesignator().obj;

		if (designator.getKind() != Obj.Var && designator.getKind() != Obj.Elem) {
			report_error("Greska na liniji " + readStmt.getDesignator().getLine()
					+ " : unutar read funkcije mogu se naci promenljiva ili element niza!", null);
		}

		if (designator.getType() != Tab.intType && designator.getType() != Tab.charType
				&& designator.getType() != boolType) {
			report_error("Greska na liniji " + readStmt.getDesignator().getLine()
					+ " : unutar funkcije read mogu biti samo int, char ili bool tip!", null);
		}

	}

	public void visit(Return retVoid) {

		if (currentMethod == null) {
			report_error("Greska na liniji " + retVoid.getLine() + " : "
					+ "return ne sme postojati izvan tela metoda odnosno globalnih funkcija!", null);
		}
		Struct currMethodType = currentMethod.getType();
		if (currMethodType != Tab.noType) {
			report_error("Nedostaje povratna vrednost funkcije!", retVoid);
		}

	}

	public void visit(IfStatement cond) {

		Struct condition = cond.getCondition().struct;
		if (condition != boolType) {
			report_error("Greska na liniji " + cond.getLine() + " : uslov unutar zagrada mora biti bool tipa!", null);
		}
	}

	public void visit(CondFactExpr condFact) {
		condFact.struct = condFact.getExpr().struct;
	}

	public void visit(CondFactRelop condFact) {

		Struct te = condFact.getExpr().struct;
		Struct t = condFact.getExpr1().struct;

		if (!te.compatibleWith(t)) {
			report_error("Greska na liniji " + condFact.getLine() + " : "
					+ "izrazi za leve i dense strane relacionog operatora moraju biti kompatibilni!", null);
			condFact.struct = Tab.noType;
		} else {
			condFact.struct = boolType;
		}

	}

	// public void visit(ConditionTermsList cond) {
	//
	// cond.struct = cond.getCondTerm().struct;
	//
	// }

	public void visit(CondTerm condTerm) {

		condTerm.struct = condTerm.getCondFact().struct;
	}

	public void visit(ConditionTerm condition) {
		condition.struct = condition.getCondTerm().struct;
	}

	public void visit(OrCondition condition) {
		condition.struct = condition.getCondTerm().struct;
	}

	public void visit(SquareExpr expr) {

		Struct ex = expr.getExpr().struct;

		if (ex != Tab.intType) {
			report_error("Greska na liniji " + expr.getLine() + " : mora biti tipa int!", null);
		}

	}

	public void visit(FormalParams fp) {

		Tab.insert(Obj.Var, fp.getParamName(), fp.getType().struct);
		int lvl = currentMethod.getLevel() + 1;
		currentMethod.setLevel(lvl);

	}

	public void visit(ParametarsList fp) {

		Tab.insert(Obj.Var, fp.getParamName(), fp.getType().struct);
		int lvl = currentMethod.getLevel() + 1;
		currentMethod.setLevel(lvl);

	}

	public void visit(Expressions ex) {

		// actparametars.add(ex.getExpr());
		if(!actpars.isEmpty()) {
		actpars.peek().add(ex.getExpr().struct);
		}
	}

	public void visit(Ex ex) {

		// actparametars.add(ex.getExpr());
		
		if(!actpars.isEmpty()) {
		actpars.peek().add(ex.getExpr().struct);
		}

	}

	public void visit(NamespaceName namespace) {

		if (!namespaces.contains(namespace.getNamespaceName())) {
			namespaces.add(namespace.getNamespaceName());
		} else {
			report_error("Greska na liniji: " + namespace.getLine() + " vec postoji namespace sa imenom "
					+ namespace.getNamespaceName(), null);
		}

		inNamespace = true;
		currNamespace = namespace.getNamespaceName();
	}

	public void visit(ForStmt loop) {
		inFor--;

		if (loop.getCondList().getClass() == ConditionList.class) {
			if (((ConditionList) loop.getCondList()).getCondFact().struct != boolType) {
				report_error("Greska na liniji: " + loop.getLine() + " Uslov u for petlji mora biti bool tipa!", null);
			}
		}
	}

	public void visit(ForLoop loop) {
		inFor++;
	}

	public void visit(Break br) {
		if (inFor == 0) {

			report_error("Greska na liniji: " + br.getLine() + " : break van for petlje!", null);

		}

	}

	public void visit(Continue br) {
		if (inFor == 0) {

			report_error("Greska na liniji: " + br.getLine() + " : break van for petlje!", null);

		}

	}

	public void visit(FactorExpr factorExpr) {

		factorExpr.struct = factorExpr.getExpr().struct;

	}

	public void visit(Namespace namespace) {

		inNamespace = false;
		currNamespace = null;
	}

	public boolean passed() {
		return !errorDetected;
	}

}
