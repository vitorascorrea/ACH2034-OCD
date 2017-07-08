import java.util.Locale;
import java.util.Scanner;

public class CPU{
  //Usamos int[] para passar os registradores por referência
  //Cada operação e operando tem 1 byte, portanto uma instrução MOV tem, por exemplo, 3 bytes. Os valores então podem ir de -127 até 128.
  private String[] memory;

  private int memory_avaliability;

  //Registradores de uso geral
  // AX (acumulador, usado em operações aritimeticas)
  private int ax = 0;
  // BX (ponteiro para acessar a memoria, auxilia nas operações aritmeticas)
  private int bx = 0;
  // CX (contador)
  private int cx = 0;
  // DX (usado em operações aritimeticas, recebe o resto da divisão e o produto da multiplicação)
  private int dx = 0;

  //Outros Registradores
  // PC (contém o endereço a ser buscado na próxima instrução)
  private int pc = 0;
  // MAR (contém o endereço de uma posição da memória)
  private String mar = "";
  // MBR (contém uma palavra a ser escrita na memória ou a lida mais recentemente)
  private int mbr = 0;
  // IR (contém a última instrução buscada)
  private int ir = 0;

  //Flags
  // Zero-flag
  private int zf = 0;
  // Sign-flag
  private int sf = 0;
  // Overflow-flag
  private int of = 0;

  public CPU(int memory_size){
    setMemorySize(memory_size);
  }

  public void setMemorySize(int size){
    this.memory = new String[size];
    increaseMemoryAvaliability(size);
  }

  public String fetchFromMemory(){
    int current_pc = this.getPc();
    String fetch = memory[current_pc];
    if(fetch != null && !isNumber(fetch)){
      this.setIr(current_pc - 1);
    }
    this.setPc(current_pc + 1);
    return fetch;
  }

  public String[] getMemory(){
    return memory;
  }

  public int getMemoryAvaliability() {
		return memory_avaliability;
	}

	public void decreaseMemoryAvaliability(int dec) {
		this.memory_avaliability -= dec;
	}

  public void increaseMemoryAvaliability(int inc) {
		this.memory_avaliability += inc;
	}

  public boolean pushIntoMemory(String operation, String operand1, String operand2, int num_bytes) {
    //Checamos se há espaço em memória e armazenamos a operação e os operandos
    if(this.getMemoryAvaliability() >= num_bytes){
      int aux = 0;
      for(int i = 0; i < this.memory.length; i++){
        if(this.memory[i] == null){
          if(aux == 0){
            this.memory[i] = operation + "," + num_bytes; //Salvamos também o número de bytes daquela instrução para facilitar o fetch
            decreaseMemoryAvaliability(1);
          } else if(aux == 1){
            this.memory[i] = operand1;
            decreaseMemoryAvaliability(1);
          } else if(aux == 2 && operand2 != null){
            this.memory[i] = operand2;
            decreaseMemoryAvaliability(1);
            System.out.println("Instrução " + operation + " " + operand1 + ", " + operand2 + " inserida na memória na posição " + (i - 2) + " - parametros nas posições " + (i - 1) + " e " + i);
            break;
          }

          if(aux == 1 && operand2 == null){
            System.out.println("Instrução " + operation + " " + operand1 + " inserida na memória na posição " + (i - 1) + " - parametro na posição " + i);
            break;
          }else{
            aux++;
          }

        }
      }
      return true;
    }else{
      return false;
    }
	}

  public boolean isNumber(String candidate){
    try{
      int aux = Integer.parseInt(candidate);
    }catch(Exception e){
      return false;
    }
    return true;
  }

  public void mov(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      src_val = Integer.parseInt(this.memory[src_memory_index]);
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
      this.setMbr(src_val);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }


    if(dst_reg != ""){
      this.setRegVal(dst_reg, src_val);
    }else{
      this.memory[dst_memory_index] = Integer.toString(src_val);
    }
  }

  public void cmp(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_val = Integer.parseInt(this.memory[dst_memory_index]);
      dst_reg = "";
    }else if(isNumber(instruction[1])){ //é uma constante
      dst_val = Integer.parseInt(instruction[1]);
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      src_val = Integer.parseInt(this.memory[src_memory_index]);
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }

    if(src_val == dst_val){
      this.setZf(1);
    }else{
      this.setZf(0);
    }

  }

  public void inc(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    int op_result = dst_val + 1;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void dec(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    int op_result = dst_val - 1;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void and(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[2].replace("[","").replace("]",""))]);
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }

    int op_result = dst_val & src_val;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void or(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[2].replace("[","").replace("]",""))]);
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }

    int op_result = dst_val | src_val;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void add(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[2].replace("[","").replace("]",""))]);
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }

    int op_result = dst_val + src_val;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void sub(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;
    int src_val;
    int src_memory_index = 0;
    String src_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
      dst_reg = "";
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
      dst_reg = instruction[1];
    }

    if(instruction[2].contains("[")){ //é um endereço de memória
      src_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[2].replace("[","").replace("]",""))]);
      src_memory_index = Integer.parseInt(instruction[2].replace("[","").replace("]",""));
      this.setMbr(src_val);
      src_reg = "";
    }else if(isNumber(instruction[2])){ //é uma constante
      src_val = Integer.parseInt(instruction[2]);
    }else{ //é um registrador
      src_val = this.getRegVal(instruction[2]);
      this.setMbr(src_val);
      src_reg = instruction[2];
    }

    int op_result = dst_val - src_val;

    if(dst_reg != ""){
      this.setRegVal(dst_reg, op_result);
    }else{
      this.memory[dst_memory_index] = Integer.toString(op_result);
    }

    this.updateFlags(op_result);
  }

  public void mul(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
    }else if(isNumber(instruction[1])){ //é uma constante
      dst_val = Integer.parseInt(instruction[1]);
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
    }
    int op_result = dst_val * this.getRegVal("A");
    this.setMbr(op_result);

    //A operação sempre será com o registrador Ax
    this.setRegVal("A", op_result);

    this.updateFlags(op_result);
  }

  public void div(String[] instruction) throws Exception{
    int dst_val;
    int dst_memory_index = 0;
    String dst_reg;

    if(instruction[1].contains("[")){ //é um endereço de memória
      dst_val = Integer.parseInt(this.memory[Integer.parseInt(instruction[1].replace("[","").replace("]",""))]);
      dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
      this.setMar("[" + dst_memory_index + "]");
    }else if(isNumber(instruction[1])){ //é uma constante
      dst_val = Integer.parseInt(instruction[1]);
    }else{ //é um registrador
      dst_val = this.getRegVal(instruction[1]);
      this.setMar("[" + instruction[1] + "]");
    }
    int op_result = dst_val / this.getRegVal("A");
    this.setMbr(op_result);

    //A operação sempre será com o registrador Ax
    this.setRegVal("A", op_result);

    this.updateFlags(op_result);
  }

  public void jmp(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    this.setPc(dst_memory_index);
  }

  public void je(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getZf() == 1){
      this.setPc(dst_memory_index);
    }
  }

  public void jne(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getZf() == 0){
      this.setPc(dst_memory_index);
    }
  }

  public void jg(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getZf() == 0 && this.getSf() == this.getOf()){
      this.setPc(dst_memory_index);
    }
  }

  public void jge(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getSf() == this.getOf()){
      this.setPc(dst_memory_index);
    }
  }

  public void jl(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getSf() != this.getOf()){
      this.setPc(dst_memory_index);
    }
  }

  public void jle(String[] instruction) throws Exception{
    //O parametro sempre será um endereço de memória
    int dst_memory_index = Integer.parseInt(instruction[1].replace("[","").replace("]",""));
    if(this.getZf() == 1 && this.getSf() != this.getOf()){
      this.setPc(dst_memory_index);
    }
  }

  public void updateFlags(int result){
    this.setZf(result == 0 ? 1 : 0);
    this.setSf(result < 0 ? 1 : 0);
    this.setOf(result < -127 || result > 128 ? 1 : 0);
  }

  public void setRegVal(String dst_reg, int src){
    if(dst_reg.equals("A")) this.setAx(src);
    if(dst_reg.equals("B")) this.setBx(src);
    if(dst_reg.equals("C")) this.setCx(src);
    if(dst_reg.equals("D")) this.setDx(src);
  }

  public int getRegVal(String reg){
    if(reg.equals("A")) return this.getAx();
    if(reg.equals("B")) return this.getBx();
    if(reg.equals("C")) return this.getCx();
    if(reg.equals("D")) return this.getDx();
    else return -1;
  }

  public void printSystemStatus(){
    System.out.println("Registradores de uso geral:");
    System.out.println("AX: " + this.getAx() + " - BX: " + this.getBx() + " - CX: " + this.getCx() + " - DX: " + this.getDx());
    System.out.println("Registradores de uso específico:");
    System.out.println("PC: [" + this.getPc() + "] - IR: [" + this.getIr() + "] - MBR: " + this.getMbr() + " - MAR: " + this.getMar());
    System.out.println("Flags:");
    System.out.println("ZF: " + this.getZf() + " - SF: " + this.getSf() + " - OF: " + this.getOf());
  }

  public int getAx() {
		return ax;
	}

	public void setAx(int ax) {
		this.ax = ax;
	}

	public int getBx() {
		return bx;
	}

	public void setBx(int bx) {
		this.bx = bx;
	}

	public int getCx() {
		return cx;
	}

	public void setCx(int cx) {
		this.cx = cx;
	}

	public int getDx() {
		return dx;
	}

	public void setDx(int dx) {
		this.dx = dx;
	}

	public int getPc() {
		return pc;
	}

	public void setPc(int pc) {
		this.pc = pc;
	}

	public String getMar() {
		return mar;
	}

	public void setMar(String mar) {
		this.mar = mar;
	}

	public int getMbr() {
		return mbr;
	}

	public void setMbr(int mbr) {
		this.mbr = mbr;
	}

	public int getIr() {
		return ir;
	}

	public void setIr(int ir) {
		this.ir = ir;
	}

	public int getZf() {
		return zf;
	}

	public void setZf(int zf) {
		this.zf = zf;
	}

	public int getSf() {
		return sf;
	}

  public void setSf(int sf) {
		this.sf = sf;
	}

	public int getOf() {
		return of;
	}

	public void setOf(int of) {
		this.of = of;
	}
}
