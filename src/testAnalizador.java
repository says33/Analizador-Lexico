/**
 * Created by IntelliJ IDEA.
 * User: saysrodriguez
 * Date: 12/03/12
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import  java.io.*;

public class testAnalizador {

    public static void main(String args[]) throws IOException{
        String expr;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Parser p = new Parser();

        for (;;){
            System.out.print("Enter expression: ");
            expr = br.readLine();
                    try{
                        System.out.println("Resultado: " + p.evaluate(expr));
                        System.out.println();
                    }catch (ParserException exc) {
                        System.out.println(exc);
                    }
                }

         }
    }


