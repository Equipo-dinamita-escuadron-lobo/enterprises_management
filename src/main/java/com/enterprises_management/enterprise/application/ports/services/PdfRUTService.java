package com.enterprises_management.enterprise.application.ports.services;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import com.enterprises_management.enterprise.application.ports.output.PdfRUTContentOutput;
import com.enterprises_management.enterprise.application.ports.input.PdfRUTContent;


@Service
public class PdfRUTService {
    
    public PdfRUTContentOutput extractContent(PdfRUTContent request) throws IOException {
        File tempFile = File.createTempFile("upload", ".pdf");

        // Transferir el archivo recibido a un archivo temporal
        request.getFile().transferTo(tempFile);

        try (PDDocument document = PDDocument.load(tempFile,"1")) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);  
            String content = pdfStripper.getText(document);
            String typeId = "";
            int idPerson = 0;
            String razonSocial="";
            String names="";
            String lastNames="";
            String [] ubication = null;
            String pais = "";
            String departamento = "";
            String ciudad = "";
            String direccion = "";
            String correo = "";
            long cell = 0;
            try{
                String[] extractedLines = extractAfterClasificacion(content);
                String[] extractedUbication = extractUbicationThird(content);
                String[] personaJuridica;
                String[] personaNatural;
                String typePerson;
                String[] aux = separateNumbersAndText(extractedLines[3]);
                typePerson = aux[0];
                if(extractedLines[3].contains("Persona jurídica")){
                    //Extraer la primera parte de identificacion para persona juridica 
                    typeId = "NIT";
                    personaJuridica = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaJuridica[0]).length() > 0 ? String.valueOf(personaJuridica[0]).substring(0, String.valueOf(personaJuridica[0]).length() - 1) : String.valueOf(personaJuridica[0]));
                    razonSocial = extractedLines[5];
                }else{
                    //Extraer la primera parte de identificacion para persona natural
                    String [] aux1 = separateNumbersAndText(cleanString(extractedLines[3]));
                    typeId = aux1[2];
                    typeId = typeId.trim();
                    personaNatural = separateNumbersAndText(cleanString(extractedLines[2]));
                    idPerson = Integer.parseInt(String.valueOf(personaNatural[0]).length() > 0 ? String.valueOf(personaNatural[0]).substring(0, String.valueOf(personaNatural[0]).length() - 1) : String.valueOf(personaNatural[0]));
                    String[] datos = new String[4];
                    datos = splitBySpaceAndUpperCase(extractedLines[5]);
                    lastNames = datos[0]+" "+ datos[1];
                    names = datos[2]+" "+ datos[3];
                }
                ubication =  separateNumbersAndText(cleanString(extractedUbication[0]));
                pais = cleanString(ubication[0]);
                departamento = cleanString(ubication[2]);
                ciudad = cleanString(ubication[4]);
                direccion = extractedUbication[1];
                correo = extractedUbication[2];
                String [] contact = separateAndJoinNumbers(extractedUbication[3]);
                cell = Long.parseLong(contact[1]);
                System.out.println("Tipo de persona: "+typePerson);
                System.out.println("Tipo de identifiacion: "+ typeId);
                System.out.println("Numero Identificacion: "+ idPerson);
                System.out.println("Razon social: "+razonSocial);
                System.out.println("Apellidos: "+lastNames);
                System.out.println("Nombres: "+names);
                System.out.println("Pais: "+pais);
                System.out.println("Departamento: "+departamento);
                System.out.println("Ciudad: "+ciudad);
                System.out.println("Direccion: "+direccion);
                System.out.println("Correo: "+correo);
                System.out.println("Telefono: "+cell);
                String infoThird = typePerson+";"+typeId+";"+idPerson+";"+razonSocial+";"+lastNames+";"+names+";"+pais+";"+departamento+";"+ciudad+";"+direccion+";"+correo+";"+cell;
                return new PdfRUTContentOutput(infoThird);
            }catch(Exception e){
                System.err.println("Error processing PDF content: " + e.getMessage());
                e.printStackTrace();
            }
            String infoThird = ""+";"+typeId+";"+idPerson+";"+razonSocial+";"+lastNames+";"+names+";"+pais+";"+departamento+";"+ciudad+";"+direccion+";"+correo+";"+cell;
            return new PdfRUTContentOutput(infoThird);
        } finally {
            tempFile.delete();
        }
    }

    private String[] extractAfterClasificacion(String content) {
        int index = content.indexOf("CLASIFICACIÓN");
        if (index != -1) {
            String result = content.substring(index + "CLASIFICACIÓN".length()).trim();
            String[] lines = result.split("\\r?\\n"); 
            return lines;
        }
        return new String[0];
    }

    private String[] extractUbicationThird(String content) {
        int index = content.lastIndexOf("COLOMBIA");
        if (index != -1) {
            
            String result = content.substring(index).trim();
            String[] lines = result.split("\\r?\\n"); 
            return lines;
        }
        return new String[0];
    }
    
    

    public static String cleanString(String input) {
        String cleaned = input.replaceAll("\\s*\\n\\s*", "\n") // Limpiar saltos de linea con espacios
                              .replaceAll("\\s{2,}", " ")    // Reemplaza multiples espacios por uno
                              .trim();                       // Elimina espacios al principio y al final
        // Elimina espacios entre varios numeros consecutivos
        cleaned = cleaned.replaceAll("(\\d)\\s+(?=\\d)", "$1");
        return cleaned;
    }

    public static String[] separateNumbersAndText(String input) {
        // Regex para separar cuando hay un cambio de numeros a letras
        return input.split("(?<=\\d)(?=\\D)|(?<=\\D)(?=\\d)");
    }

    public static String[] separateAndJoinNumbers(String input) {
        // Reemplaza multiples espacios con un separador especial
        String modifiedInput = input.replaceAll("\\s{2,}", ",");
        // Reemplaza espacios simples entre numeros
        modifiedInput = modifiedInput.replaceAll("\\s+", "");
        // Divide en un vector usando la coma como delimitador
        return modifiedInput.split(",");
    }

    public static String[] splitBySpaceAndUpperCase(String input) {
        return input.split("(?<=\\s)(?=[A-Z])");
    }
    
}