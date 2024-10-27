package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{

	// ukljucivanje informacije o poziciji tokena, pravi token na osnovu tipa
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena, pravi token na osnovu vrednosti i tipa
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }



"program"   { return new_symbol(sym.PROG, yytext()); }
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"return" 	{ return new_symbol(sym.RETURN, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"break" 		{ return new_symbol(sym.BREAK, yytext()); }
"else" 		{ return new_symbol(sym.ELSE, yytext()); }
"const" 		{ return new_symbol(sym.CONST, yytext()); }
"namespace"   { return new_symbol(sym.NAMESPACE, yytext()); }
"static"   { return new_symbol(sym.STATIC, yytext()); }
"for"   { return new_symbol(sym.FOR, yytext()); }
"read"   { return new_symbol(sym.READ, yytext()); }
"if" 		{ return new_symbol(sym.IF, yytext()); }
"continue" 	{ return new_symbol(sym.CONTINUE, yytext()); }
"new" 		{ return new_symbol(sym.NEW, yytext()); }
"++" 		{ return new_symbol(sym.INC, yytext()); }
"--"		{ return new_symbol(sym.DEC, yytext()); }
"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"-" 		{ return new_symbol(sym.MINUS, yytext()); }
"*" 		{ return new_symbol(sym.MUL, yytext()); }
"/" 		{ return new_symbol(sym.DIV, yytext()); }
"%" 		{ return new_symbol(sym.MOD, yytext()); }
"=>" 		{ return new_symbol(sym.ARR, yytext()); }
"==" 		{ return new_symbol(sym.EQUALTO, yytext()); }
"!=" 		{ return new_symbol(sym.NOTEQUAL, yytext()); }
">=" 		{ return new_symbol(sym.GETH, yytext()); }
"<=" 		{ return new_symbol(sym.LETH, yytext()); }
"=" 		{ return new_symbol(sym.EQUAL, yytext()); }
">" 		{ return new_symbol(sym.GTH, yytext()); }
"<" 		{ return new_symbol(sym.LTH, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
":" 		{ return new_symbol(sym.COLON, yytext()); }
"." 		{ return new_symbol(sym.DOT, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"(" 		{ return new_symbol(sym.LPAREN, yytext()); }
")" 		{ return new_symbol(sym.RPAREN, yytext()); }
"{" 		{ return new_symbol(sym.LBRACE, yytext()); }
"}"			{ return new_symbol(sym.RBRACE, yytext()); }
"]" 		{ return new_symbol(sym.RSQUARE, yytext()); }
"["			{ return new_symbol(sym.LSQUARE, yytext()); }
"&&" 		{ return new_symbol(sym.AND, yytext()); }
"||" 		{ return new_symbol(sym.OR, yytext()); }

"true"      { return new_symbol(sym.BOOL, true); }
"false"		{ return new_symbol(sym.BOOL, false); }



<YYINITIAL> "//" 		     { yybegin(COMMENT); }
<COMMENT> .      { yybegin(COMMENT); }
<COMMENT> "\r\n" { yybegin(YYINITIAL); }


[0-9]+  { return new_symbol(sym.NUMBER,  Integer.parseInt(yytext())); }
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 	{return new_symbol (sym.IDENT, yytext()); }
'.'  {return new_symbol(sym.CHAR, yytext().charAt(1)); }



. { System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1)+" i koloni "+(yycolumn)); }