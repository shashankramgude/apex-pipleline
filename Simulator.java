import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class Simulator {
	private static String fileName = "";
	//<pc,instruction>
	private static Map<Integer,String> instructions = new LinkedHashMap<Integer,String>();
	//<reg_name, value> 
	private static Map<String,Integer> registers = new LinkedHashMap<String,Integer>();
	private static List<String> instructionSet = new ArrayList<String>();
	private static int giveNoOfCycles = 0;
	private static int noOfCycles = 0;
	private static int pc = 20000;
	private static Integer[] memoryAddress = new Integer[10000];// {0};
	private static String fetch = "";
	private static String decode = "";
	private static String execution = "";
	private static String memory = "";
	private static String writeBack = "";
	private static Map<String, Stage> stages = new LinkedHashMap<String, Stage>();
	private static boolean isHalt = false;
	private static boolean isBranch = false;
	public static void main(String []args){
	boolean exit = false;
		fileName = args[0];
		try {
			while(!exit){
				System.out.println("\n\n5 Stage APEX Simulator");
				System.out.println("1. Initialize");
				System.out.println("2. Simulate");
				System.out.println("3. Display");
				System.out.println("Enter your choice : ");
				Scanner in = new Scanner(System.in);
				String msg = in.nextLine();
				int input = Integer.valueOf(msg);
				switch (input) {
				case 1:
					initialize();
					break;
				case 2:
					System.out.println("\nEnter no. of cycles : ");
					msg = in.nextLine();
					giveNoOfCycles = giveNoOfCycles + Integer.valueOf(msg);
					simulate();
					break;
	
				case 3:
					display();
					break;

				case 4:
					exit = true;
					break;
		
				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
	}
	
	public static void initialize(){
		noOfCycles = 0;
		pc = 20000;
		giveNoOfCycles = 0;
		//set memory
		for(int i = 0;i<memoryAddress.length;i++){
			memoryAddress[i] = 0;
		}
		//set stages
		stages.put("F", null);
		stages.put("D", null);
		stages.put("E", null);
		stages.put("M", null);
		stages.put("W", null);
		
		//set registers
		for(int i = 0;i < 8;i++){
			registers.put("R"+i,0);
		}
		registers.put("X", 0);
		//set instructions
		instructionSet.add("ADD");
		instructionSet.add("SUB");
		instructionSet.add("MOVC");
		instructionSet.add("MUL");
		instructionSet.add("AND");
		instructionSet.add("OR");
		instructionSet.add("EX-OR");
		instructionSet.add("LOAD");
		instructionSet.add("STORE");
		instructionSet.add("BZ");
		instructionSet.add("BNZ");
		instructionSet.add("JUMP");
		instructionSet.add("BAL");
		instructionSet.add("HALT");
		
		//read instruction file
		BufferedReader br = null;
		try {
			String currentLine = "";
			br = new BufferedReader(new FileReader(fileName));
			while ((currentLine = br.readLine()) != null) {
				instructions.put(pc, currentLine);
				pc++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		pc = 20000;
	}
	
	public static void simulate(){
		//pc = 20000;
		while((!isHalt) && (noOfCycles<giveNoOfCycles)){
			noOfCycles++;
			fetch = "No Op";
			decode = "No Op";
			execution = "No Op";
			memory = "No Op";
			writeBack = "No Op";
			if(isDependecy()){
				getDecodedString(stages.get("D"));
				writeBack();
				if(isHalt){
					break;
				}
				stages.put("M", null);
				memory();
				stages.put("E", null);
			}else{
				writeBack();
				if(isHalt){
					break;
				}
				memory();
				isBranch = false;
				execution();
				if(!isBranch){
					decode();
					fetch();
					pc++;
				}
			}
		}
	}
	
	private static void display() {
		System.out.println("Cycle "+noOfCycles);
		System.out.println("Fetch Stage : "+fetch);
		System.out.println("Decode Stage : "+decode);
		System.out.println("Execution Stage : "+execution);
		System.out.println("Memory Stage : "+memory);
		System.out.println("Write Back Stage : "+writeBack);
		System.out.println("Memory :");
		for(int i = 0 ;i<20;i++){
			System.out.println(i+" : "+memoryAddress[i]+"\t\t"+(i+20)+" : "+memoryAddress[i+20]+"\t\t"+(i+40)+" : "+memoryAddress[i+40]+"\t\t"+(i+60)+" : "+memoryAddress[i+60]+"\t\t"+(i+80)+" : "+memoryAddress[i+80]);
		}
		System.out.println("Registers : ");
		for (String key : registers.keySet()) {
			System.out.println(key+":"+registers.get(key));
		}
	}

	private static void fetch(){
		String instruction = instructions.get(pc);
		fetch = "No Op";
		if(instruction!=null){
			fetch = instruction;
		}
		Stage stage = new Stage();
		stage.setInstruction(instruction);
		stages.put("F", stage);
	}
	
	private static boolean decode(){
		Stage stage = stages.get("F");
		if(stage!=null){	
			if(stage.getInstruction()!=null){
				String tempInstruction[] = stage.getInstruction().split(" ");
				String operation = tempInstruction[0].toUpperCase();
				if(!tempInstruction[0].equalsIgnoreCase("HALT")){
					tempInstruction[1] = tempInstruction[1].trim();
				}
				decode = "Decoded : "+stage.getInstruction()+"\n";
				List<String> operandsList = new ArrayList<String>();
				for(int i = 1;i<tempInstruction.length;i++){
					operandsList.add(tempInstruction[i].toUpperCase());
					decode = decode + tempInstruction[i]+":"+getOperandValue(tempInstruction[i].toUpperCase());
				}
//				String operands[] = tempInstruction[1].split(",");
//				for(int i=0;i<operands.length;i++){
//					operands[i] = operands[i].toUpperCase();
//				}
				//List<String> operandsList = new ArrayList<String>(Arrays.asList(operands));
				stage.setOperands(operandsList);
				stage.setOperation(operation);
				getDecodedString(stage);
				if(operation.equalsIgnoreCase("MOV")){
					operation = "ADD";
					operandsList.add("0");
				}
				stage.setOperands(operandsList);
				stage.setOperation(operation);
				if(!tempInstruction[0].equalsIgnoreCase("HALT")){
					stage.setDestReg(operandsList.get(0));
				}
			}	
		}
		stages.put("D",stage);
		return true;
	}
	
	private static void execution(){
		Stage stage = stages.get("D");
		if(stage!=null){ 
			String operation = stage.getOperation();
			List<String> operands = stage.getOperands();
			if(stage.getInstruction()!=null){
				//remove
				//pc++;
				execution = "Instruction : "+stage.getInstruction()+"\n";
				execution = execution + "Operation : "+operation+"\n";
				if(operation.equalsIgnoreCase("ADD")){
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
						stage.setResult(registers.get(operands.get(1)) + getOperandValue(operands.get(2)));
						//registers.put(operands.get(0), registers.get(operands.get(1)) + registers.get(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("SUB")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
						stage.setResult(registers.get(operands.get(1)) - getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("MUL")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
						stage.setResult(registers.get(operands.get(1)) * getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("AND")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){
						stage.setResult(registers.get(operands.get(1)) & getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("OR")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){
						stage.setResult(registers.get(operands.get(1)) | getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("EX-OR")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){
						stage.setResult(registers.get(operands.get(1)) ^ getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("LOAD")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
						stage.setResult(registers.get(operands.get(1)) + getOperandValue(operands.get(2)));
						//registers.put(operands.get(0), registers.get(operands.get(1)) + registers.get(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("STORE")) {
					if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
						stage.setResult(registers.get(operands.get(1)) + getOperandValue(operands.get(2)));
					}
				}else if (operation.equalsIgnoreCase("BZ")) {
					Stage exStage = stages.get("E");
					if(getOperandValue(exStage.getOperands().get(0)) == 0){
						stage.setResult(pc + getOperandValue(stage.getOperands().get(0)));
						pc = pc + getOperandValue(stage.getOperands().get(0));
						stages.put("D", null);
						stages.put("F", null);
						isBranch = true;
					}
				}else if (operation.equalsIgnoreCase("BNZ")) {
					Stage exStage = stages.get("E");
					if(getOperandValue(exStage.getOperands().get(0)) != 0){
						stage.setResult(pc + getOperandValue(stage.getOperands().get(0)));
						pc = pc + getOperandValue(stage.getOperands().get(0));
						stages.put("D", null);
						stages.put("F", null);
						isBranch = true;
					}
				}else if (operation.equalsIgnoreCase("JUMP")) {
					if((registers.get(operands.get(0)) != null)){
						stage.setResult(registers.get(operands.get(0)) + getOperandValue(operands.get(1)));
						pc = registers.get(operands.get(0)) + getOperandValue(operands.get(1));
						stages.put("D", null);
						stages.put("F", null);
						isBranch = true;
					}
				}else if (operation.equalsIgnoreCase("BAL")) {
					//to-do
					registers.put("X", pc+1);
					if((registers.get(operands.get(0)) != null)){
						stage.setResult(registers.get(operands.get(0)) + getOperandValue(operands.get(1)));
						pc = registers.get(operands.get(0)) + getOperandValue(operands.get(1));
						stages.put("D", null);
						stages.put("F", null);
						isBranch = true;
					}
				}else if (operation.equalsIgnoreCase("HALT")) {
					
				}
				execution = execution + "Result : "+stage.getResult();
			}
		}	
		stages.put("E", stage);
	}

	private static void memory(){
		Stage stage = stages.get("E");
		if(stage!=null){	
			if(stage.getInstruction()!=null){
				memory = "Instruction : "+stage.getInstruction();
				String operation = stage.getOperation();
				List<String> operands = stage.getOperands();
				if(operation.equalsIgnoreCase("MOVC")) {
					if(registers.get(operands.get(0)) != null){
						stage.setResult(Integer.valueOf(operands.get(1)));
						//registers.put(operands.get(0), Integer.valueOf(operands.get(1).split("#")[1]));
						memory = memory + operands.get(0)+":"+registers.get(operands.get(0));
					}
				}else if(operation.equalsIgnoreCase("LOAD")) {
					if(registers.get(operands.get(0)) != null){
						stage.setResult(memoryAddress[stage.getResult()]);
						memory = memory + "Result:"+stage.getResult();
						//registers.put(operands.get(0), Integer.valueOf(operands.get(1).split("#")[1]));
					}
				}else if(operation.equalsIgnoreCase("STORE")){
					if(registers.get(operands.get(0)) != null){
						//stage.setResult(memoryAddress[stage.getResult()]);
						memoryAddress[stage.getResult()] = registers.get(stage.getOperands().get(0));
						memory = memory + "Result:"+stage.getResult()+"\n";
						memory = memory + "Memory :"+stage.getResult()+"->"+memoryAddress[stage.getResult()];
						//registers.put(operands.get(0), Integer.valueOf(operands.get(1).split("#")[1]));
					}
				}
			}
		}	
		stages.put("M", stage);
	}
	
	private static void writeBack(){
		Stage stage = stages.get("M");
		if(stage!=null){
			if(stage.getInstruction()!=null){
				writeBack = "Instruction : "+stage.getInstruction()+"\n";
				if(stage.getOperation().equalsIgnoreCase("BAL") || stage.getOperation().equalsIgnoreCase("BZ") || stage.getOperation().equalsIgnoreCase("BNZ") || stage.getOperation().equalsIgnoreCase("JUMP")){
					
				}else if(stage.getOperation().equalsIgnoreCase("HALT")){
					isHalt = true;
				}else if(!stage.getOperation().equalsIgnoreCase("STORE")){
					registers.put(stage.getDestReg(),stage.getResult());
					writeBack = writeBack + stage.getDestReg()+":"+stage.getResult()+"\n";
				}		
			}	
		}	
	}
	
	private static boolean isDependecy(){
		Stage exStage = stages.get("E");
		Stage memStage = stages.get("M");
		Stage decodeStage = stages.get("D");
		if((decodeStage!=null) && (decodeStage.getInstruction()!=null) && (decodeStage.getInstruction().equalsIgnoreCase("HALT"))){
			return false;
		}
		if((memStage!=null) && (memStage.getInstruction()!=null) && (memStage.getInstruction().equalsIgnoreCase("HALT"))){
			return false;
		}
		if((exStage!=null) && (exStage.getInstruction()!=null) && (exStage.getInstruction().equalsIgnoreCase("HALT"))){
			return false;
		}
		if((decodeStage!=null) && (decodeStage.getOperation()!=null) && (decodeStage.getOperation().equalsIgnoreCase("BZ"))){
			return false;
		}
		if((memStage!=null) && (memStage.getOperation()!=null) && (memStage.getOperation().equalsIgnoreCase("BZ"))){
			return false;
		}
		if((exStage!=null) && (exStage.getOperation()!=null) && (exStage.getOperation().equalsIgnoreCase("BZ"))){
			return false;
		}
		
		if((decodeStage!=null) && (decodeStage.getOperation()!=null) && (decodeStage.getOperation().equalsIgnoreCase("BNZ"))){
			return false;
		}
		if((memStage!=null) && (memStage.getOperation()!=null) && (memStage.getOperation().equalsIgnoreCase("BNZ"))){
			return false;
		}
		if((exStage!=null) && (exStage.getOperation()!=null) && (exStage.getOperation().equalsIgnoreCase("BNZ"))){
			return false;
		}
		
		if((decodeStage!=null) && (decodeStage.getOperation()!=null) && (decodeStage.getOperation().equalsIgnoreCase("BAL"))){
			return false;
		}
		if((memStage!=null) && (memStage.getOperation()!=null) && (memStage.getOperation().equalsIgnoreCase("BAL"))){
			return false;
		}
		if((exStage!=null) && (exStage.getOperation()!=null) && (exStage.getOperation().equalsIgnoreCase("BAL"))){
			return false;
		}
		
		if(exStage!=null && decodeStage!=null && decodeStage.getInstruction()!=null){
			if(decodeStage.getOperation().equalsIgnoreCase("MOVC")){
				if(exStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(1))){
					return true;
				}
			}else if(decodeStage.getOperation().equalsIgnoreCase("JUMP")){
				if(exStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(0))){
					return true;
				}
			}
			else{
				if((exStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(1))) || (exStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(2)))){
					return true;
				}
			}
		}	
		if(memStage!=null && decodeStage!=null && decodeStage.getInstruction()!=null){
			if(decodeStage.getOperation().equalsIgnoreCase("MOVC")){
				if(memStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(1))){
					return true;
				}
			}else if(decodeStage.getOperation().equalsIgnoreCase("JUMP")){
				if(memStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(0))){
					return true;
				}
			}else{
				if((memStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(1))) || (memStage.getDestReg().equalsIgnoreCase(decodeStage.getOperands().get(2)))){
					return true;
				}
			}
		}
		return false;
	}
	
	private static int getOperandValue(String operand){
		if(operand.contains("R") || operand.contains("r") || operand.contains("X") || operand.contains("x")){
			return registers.get(operand);
		}else{
			return Integer.parseInt(operand);
		}
	}
	
	private static void getDecodedString(Stage stage){
		String operation = stage.getOperation();
		List<String> operands = stage.getOperands();
		if(stage.getInstruction()!=null){
			decode = "Instruction : "+stage.getInstruction()+"\n";
			decode = decode + "Operation : "+operation+"\n"; 
			if(operation.equalsIgnoreCase("ADD") || operation.equalsIgnoreCase("SUB") || operation.equalsIgnoreCase("MUL")
					|| operation.equalsIgnoreCase("AND") || operation.equalsIgnoreCase("OR") || operation.equalsIgnoreCase("EX-OR")
					|| operation.equalsIgnoreCase("LOAD")){
				if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){
					decode = decode + "Source - "+ operands.get(1)+":"+registers.get(operands.get(1))+"\n";
					if(operands.get(2).contains("X") || operands.get(2).contains("R")){
						decode = decode +operands.get(2)+":"+getOperandValue(operands.get(2))+"\n" ;
					}else{
						decode = decode + getOperandValue(operands.get(2))+"\n" ;
					}
					decode = decode + "Destination - "+operands.get(0);
				}
			}else if (operation.equalsIgnoreCase("STORE")) {
				if((registers.get(operands.get(0)) != null) && (registers.get(operands.get(1)) != null)){// && (registers.get(operands.get(2)) != null)){
					decode = decode +  "Source - "+ operands.get(0)+":"+registers.get(operands.get(0))+"\n";
					decode = decode + operands.get(1)+":"+registers.get(operands.get(1))+"\n";
					if(operands.get(2).contains("X") || operands.get(2).contains("R")){
						decode = decode +operands.get(2)+":"+getOperandValue(operands.get(2)) +"\n";
					}else{
						decode = decode + getOperandValue(operands.get(2));
					}
				}
				////to-do
			}else if (operation.equalsIgnoreCase("BAL")) {
				registers.put("X", pc+1);
				if((registers.get(operands.get(0)) != null)){
					decode = decode + " "+operands.get(0)+":"+registers.get(operands.get(0))+"\n";
					decode = decode + " X:"+ registers.get("X")+"\n";
					decode = decode + " "+operands.get(1);
				}
			}else if(operation.equalsIgnoreCase("MOVC")) {
				if(registers.get(operands.get(0)) != null){
					decode = decode + "Source - "+Integer.valueOf(operands.get(1))+"\n";
					decode = decode + "Destination - "+ operands.get(0);
				}
			}else if(operation.equalsIgnoreCase("MOV")) {
				if(registers.get(operands.get(0)) != null){
					decode = decode + "Source - "+operands.get(1)+":"+registers.get(operands.get(1))+"\n";
					decode = decode + "Destination - "+ operands.get(0);
				}
			}else if (operation.equalsIgnoreCase("HALT")) {
			}
		}
	}
	
}
