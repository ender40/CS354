import java.util.LinkedList;import java.util.List;/* * This class is a recursive-descent parser, * modeled after the programming language's grammar. * It constructs and has-a Scanner for the program * being parsed.*/public class Parser{	private Scanner scanner;	/**	 * CONSTRUCTOR: create a Parser to parse the specified program	 * @param program program to parse	 * @return a node containing the parsed statement	 * @throws SyntaxException	 */	public Node parse(String program) throws SyntaxException{		scanner = new Scanner(program);		scanner.next();		return parseBlock();	}	//TODO: come back & look at this might need fixing	private NodeBlock parseBlock() throws SyntaxException{		List<NodeStatement> statementList = new LinkedList<>();		while(true)		{			NodeStatement statement = parseStatement();			statementList.add(statement);			if(scanner.curr().equals(new Token(";")))			{				match(";");			} else				break;		}		return new NodeBlock(statementList);		/*NodeStatement statement = parseStatement();		if(scanner.curr().equals(new Token(";")))		{			match(";");			NodeBlock blk = parseBlock();			return new NodeBlock(statement, blk);		}		return new NodeBlock(statement);*/	}	private BoolExpression parseBoolExpression() throws SyntaxException{		NodeExpr leftSide = parseExpression();		String relationalOperator = scanner.curr().lex();		match(relationalOperator);		NodeExpr rightSide = parseExpression();		return new BoolExpression(leftSide, relationalOperator, rightSide);	}	private NodeUnary parseNegative() throws SyntaxException{		int index = 1;		while(scanner.curr().lex().equals("-") || scanner.curr().lex().equals("+"))		{			if(scanner.curr().lex().equals("-"))			{				index *= - 1;				match("-");			} else				match("+");		}		if(index == - 1)		{			return new NodeUnary("-");		}		return null;	}	private NodeAssn parseAssn() throws SyntaxException{		Token id = scanner.curr();		match("id");		match("=");		NodeExpr express = parseExpression();		NodeAssn ass = new NodeAssn(id.lex(), express);		return ass;	}	private NodeExpr parseExpression() throws SyntaxException{		NodeTerm t = parseTerm();		NodeAddop add = parseAdditionSubtraction();		if(add == null)		{			return new NodeExpr(t, null, null);		}		NodeExpr express = parseExpression();		express.append(new NodeExpr(t, add, null));		return express;	}	private NodeTerm parseTerm() throws SyntaxException{		NodeFact f = parseFact();		NodeMulop mulop = parseMultiplicationDivision();		if(mulop == null)		{			return new NodeTerm(f, null, null);		}		NodeTerm t = parseTerm();		t.append(new NodeTerm(f, mulop, null));		return t;	}	private NodeFact parseFact() throws SyntaxException{		NodeUnary unary = parseNegative();		if(scanner.curr().lex().equals("("))		{			match("(");			NodeExpr expr = parseExpression();			match(")");			return new NodeFactExpr(expr, unary);		}		if(scanner.curr().equals(new Token("id")))		{			Token id = scanner.curr();			match("id");			return new NodeFactId(scanner.pos(), id.lex(), unary);		}		NodeNum n = parseDigits();		n.setUnary(unary);		match("num");		return new NodeFactNum(n);	}	private NodeStatementWr NodeStatementWr() throws SyntaxException{		match("wr");		NodeExpr xp = parseExpression();		return new NodeStatementWr(xp);	}	private NodeNum parseDigits() throws SyntaxException{		String lex = scanner.curr().lex();		return new NodeNum(lex);	}	private NodeMulop parseMultiplicationDivision() throws SyntaxException{		if(scanner.curr().equals(new Token("*")))		{			match("*");			return new NodeMulop(scanner.pos(), "*");		}		if(scanner.curr().equals(new Token("/")))		{			match("/");			return new NodeMulop(scanner.pos(), "/");		}		return null;	}	private NodeAddop parseAdditionSubtraction() throws SyntaxException{		if(scanner.curr().equals(new Token("+")))		{			match("+");			return new NodeAddop(scanner.pos(), "+");		}		if(scanner.curr().equals(new Token("-")))		{			match("-");			return new NodeAddop(scanner.pos(), "-");		}		return null;	}	/**	 * Parse a program's statement	 * @return the parsed statement	 * @throws SyntaxException if the Scanner gets an undefined token	 */	private NodeStatement parseStatement() throws SyntaxException{		if(scanner.curr().lex().equals("wr"))		{			return NodeStatementWr();			/*match("wr");			NodeExpr xp = parseExpression();			return new NodeStatementWr(xp);*/		} else if(scanner.curr().lex().equals("rd"))		{			match("rd");			Token t = scanner.curr();			match("id");			return new NodeStatementRd(t.lex());		} else if(scanner.curr().lex().equals("if"))		{			match("if");			BoolExpression boolExpression = parseBoolExpression();			match("then");			NodeStatement ifThenexpression = parseStatement();			if(scanner.curr().lex().equals("else"))			{				match("else");				NodeStatement elseStatement = parseStatement();				return new NodeStatementIfThenElseBlock(boolExpression, ifThenexpression, elseStatement);			} else			{				return new NodeStatementIfThenBlock(boolExpression, ifThenexpression);			}		} else if(scanner.curr().lex().equals("begin"))		{			match("begin");			NodeBlock statement = parseBlock();			match("end");			return statement;		} else if(scanner.curr().lex().equals("while"))		{			match("while");			BoolExpression boolExpression = parseBoolExpression();			match("do");			NodeStatement whileStatement = parseStatement();			return new NodeStatementWhile(boolExpression, whileStatement);		} else		{			return new NodeStatementAssn(parseAssn());		}	}	private void match(String s) throws SyntaxException{		scanner.match(new Token(s));	}}