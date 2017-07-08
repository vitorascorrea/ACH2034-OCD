import java.util.Locale;
import java.util.Scanner;
import java.io.File;

public class EP2OCD{
  public static void main(String[] args){
    try{
      Scanner options = new Scanner(System.in);
      System.out.println("Executar em qual modo?");
      System.out.println("1 - Ler arquivo");
      System.out.println("2 - Freestyle");
      String selected_option = options.nextLine();
      CPU cpu = new CPU(Integer.parseInt(args[0])); //256 bytes de memória
      if(Integer.parseInt(selected_option) == 1){
        System.out.println();
        System.out.println("Digite o caminho do arquivo:");
        System.out.println();
        String file_path = options.nextLine();
        Scanner file = new Scanner(new File(file_path)); //Representar endereço de memória como [0..num_bytes]
        run(cpu, file);
      }else{
        runFreestyle(cpu);
      }

    }catch(Exception e){
      System.out.println(e);
    }
  }

  public static void runFreestyle(CPU cpu){
    while(true){
      loadSingleInstruction(cpu);
      executeSingleInstruction(cpu);
    }
  }

  public static void loadSingleInstruction(CPU cpu){
    Scanner file = new Scanner(System.in);
    System.out.println("Digite a sua instrução: ");
    String line = file.nextLine();
    //Primeiro pegamos a instrução
    String[] parsed_line = line.replaceAll(",", "").split(" ");
    String operation = parsed_line[0];
    String operand1 = parsed_line[1];
    String operand2 = parsed_line.length > 2 ? parsed_line[2] : null;
    //Calculamos quantos bytes ela vai ocupar na memória (1 byte para operação, 1 byte para cada operando)
    int num_bytes = 2 + (operand2 != null ? 1 : 0);
    //Tentamos colocar na memória a instrução
    if(!cpu.pushIntoMemory(operation, operand1, operand2, num_bytes)){ //Não há memória disponível para a instrução
      System.out.println("Não há memória disponível para a instrução: " + line);
    }
  }

  public static void executeSingleInstruction(CPU cpu){
    String[] instruction = fetchInstruction(cpu);
    boolean error = false;
    try{
      executeInstruction(cpu, instruction);
    }catch(Exception e){
      error = true;
    }
    fluxControl(cpu, instruction, error);
  }

  public static void loadInMemory(CPU cpu, Scanner file){
    while(file.hasNextLine()){
      String line = file.nextLine();
      //Primeiro pegamos a instrução
      String[] parsed_line = line.replaceAll(",", "").split(" ");
      String operation = parsed_line[0];
      String operand1 = parsed_line[1];
      String operand2 = parsed_line.length > 2 ? parsed_line[2] : null;
      //Calculamos quantos bytes ela vai ocupar na memória (1 byte para operação, 1 byte para cada operando)
      int num_bytes = 2 + (operand2 != null ? 1 : 0);
      //Tentamos colocar na memória a instrução
      if(!cpu.pushIntoMemory(operation, operand1, operand2, num_bytes)){ //Não há memória disponível para a instrução
        System.out.println("Não há memória disponível para a instrução: " + line);
      }
    }
  }

  public static void run(CPU cpu, Scanner file){
    loadInMemory(cpu, file);
    executeSequentially(cpu);
  }

  public static void executeSequentially(CPU cpu){
    while(true){
      String[] instruction = fetchInstruction(cpu);
      boolean error = false;
      if(instruction != null){
        try{
          executeInstruction(cpu, instruction);
        }catch(Exception e){
          error = true;
        }
        fluxControl(cpu, instruction, error);
      }else break;
    }
  }

  public static String[] fetchInstruction(CPU cpu){
    try{
      String instruction_head = cpu.fetchFromMemory();
      int instruction_size = Integer.parseInt(instruction_head.split(",")[1]);

      String[] formatted_instruction = new String[instruction_size];

      formatted_instruction[0] = instruction_head.split(",")[0];
      formatted_instruction[1] = cpu.fetchFromMemory();
      if(formatted_instruction.length > 2) formatted_instruction[2] = cpu.fetchFromMemory();
      cpu.setIr(String.join(", ", formatted_instruction).replaceFirst(", ", " "));
      return formatted_instruction;
    }catch(Exception e){
      return null;
    }
  }

  public static void executeInstruction(CPU cpu, String[] instruction) throws Exception{
    if(instruction[0].equals("MOV")){
      cpu.mov(instruction);
    }else if(instruction[0].equals("ADD")){
      cpu.add(instruction);
    }else if(instruction[0].equals("SUB")){
      cpu.sub(instruction);
    }else if(instruction[0].equals("INC")){
      cpu.inc(instruction);
    }else if(instruction[0].equals("DEC")){
      cpu.dec(instruction);
    }else if(instruction[0].equals("MUL")){
      cpu.mul(instruction);
    }else if(instruction[0].equals("DIV")){
      cpu.div(instruction);
    }else if(instruction[0].equals("CMP")){
      cpu.cmp(instruction);
    }else if(instruction[0].equals("JMP")){
      cpu.jmp(instruction);
    }else if(instruction[0].equals("JE")){
      cpu.je(instruction);
    }else if(instruction[0].equals("JNE")){
      cpu.jne(instruction);
    }else if(instruction[0].equals("JG")){
      cpu.jg(instruction);
    }else if(instruction[0].equals("JL")){
      cpu.jl(instruction);
    }else if(instruction[0].equals("JGE")){
      cpu.jge(instruction);
    }else if(instruction[0].equals("JLE")){
      cpu.jle(instruction);
    }else if(instruction[0].equals("AND")){
      cpu.and(instruction);
    }else if(instruction[0].equals("OR")){
      cpu.or(instruction);
    }
  }

  public static void fluxControl(CPU cpu, String[] instruction, boolean error){
    System.out.println();
    if(error){
      System.out.println("Ocorreu um erro na execução da instrução abaixo.");
      System.out.println();
    }
    System.out.print("Instrução: ");
    System.out.println(String.join(", ", instruction).replaceFirst(", ", " "));
    cpu.printSystemStatus();
    System.out.print("(Pressione Enter para a próxima instrução)");
    Scanner next = new Scanner(System.in);
    next.nextLine();
  }

}
