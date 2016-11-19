import java.util.ArrayList;
import java.util.List;


public class Stage {
	private String operation;
	private List<String> operands = new ArrayList<String>();
	private String destReg;
	private String instruction;
	private int result;
	
	public String getInstruction() {
		return instruction;
	}
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}
	public String getDestReg() {
		return destReg;
	}
	public void setDestReg(String destReg) {
		this.destReg = destReg;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public List<String> getOperands() {
		return operands;
	}
	public void setOperands(List<String> operands) {
		this.operands = operands;
	}
	public int getResult() {
		return result;
	}
	public void setResult(int result) {
		this.result = result;
	}
	
}
