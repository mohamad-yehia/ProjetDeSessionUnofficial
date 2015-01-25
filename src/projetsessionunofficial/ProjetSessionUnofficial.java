package projetsessionunofficial;


import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Generate a JSON String
 * Build an order manually
 */
public class ProjetSessionUnofficial {
    public static void main(String[] args) throws Exception {

        if (args.length != 2){
            System.out.println("Erreur, arguments invalides!!!");
            System.exit(0);
        }
        
        String jsonInput = args[0];// Parametre du fichier d'entre
        String jsonOutput = args[1];// Parametre du fichier de sortie
        
        String client;
        String contrat;
        String mois;
        int soin;
        String date;
        Double montant;
        double montantCalcule = 0.0;// Montant a rembourser
        
        FileWriter FileOutput = new FileWriter(jsonOutput);
        
        String claims = FileReader.loadFileIntoString(jsonInput, "UTF-8");
        JSONObject claim = JSONObject.fromObject(claims);
        
        /**  Format le montantCalcule */
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        
        contrat = claim.getString("contrat");
        client = claim.getString("client");
        mois = claim.getString("mois");
        
        JSONArray claimArray = JSONArray.fromObject(claim.getJSONArray("reclamations"));
        JSONArray reclamArray = new JSONArray();
        JSONObject reclamation = new JSONObject();       
        
        if ((verifierNumeroClient(client)) && verifierContrat(contrat) && verifierDate(claimArray, claim)) {
            
            /** Parcourir le tableau pour calculer le montant a rembourser pour chaque reclamation*/
            for (int i =0; i < claimArray.size();i ++){
                
                soin = claimArray.getJSONObject(i).getInt("soin");
                date = claimArray.getJSONObject(i).getString("date");
                montant = Double.parseDouble(claimArray.getJSONObject(i).getString("montant").replace('$', '0'));
                
                if(contrat.equals("C")){                   
                    montantCalcule = montant * 0.9;               
                }// end if C
            
                if(contrat.equals("A")){
                    if (soin == 0 || soin == 100 || soin == 200 || soin == 500){                
                        montantCalcule = montant * 0.25;
                    }
                    if ((soin >= 300 && soin <= 399) || soin == 400 || soin == 700){
                        montantCalcule = montant * 0;
                    }
                    if (soin == 600){
                        montantCalcule = montant * 0.4;
                    }
                }// end if A
        
                if(contrat.equals("B")){
                    if (soin == 0 || soin == 100 || soin == 500 || (soin >= 300 && soin <=399)){                
                        montantCalcule = montant * 0.5;
                        if(soin == 0 && montantCalcule > 40){
                            montantCalcule = 40;
                        }else if ((soin == 100 || soin == 500) && montantCalcule > 50){
                            montantCalcule = 50;
                        } 
                    }
                    if (soin == 200 || soin == 600){
                        montantCalcule = montant * 1;
                    
                        if(soin == 200 &&  montantCalcule > 70){
                            montantCalcule = 70;
                        }
                    }
                    if (soin == 700){                                  
                        montantCalcule = montant * 0.7;
                    }
                } // end if B
        
                if(contrat.equals("D")){
                    if (soin == 0 || soin == 100 || (soin >= 300 && soin <=399) || soin == 400 
                            || soin == 500 || soin == 600 || soin == 700){                
                        
                        montantCalcule = montant * 1;
                        if(soin == 0 && montantCalcule > 85){
                            montantCalcule = 85;
                        }
                        if ((soin == 100 || soin == 500) && montantCalcule > 70){
                            montantCalcule = 75;
                        }
                        if((soin == 200 || soin == 600) && montantCalcule > 100){
                            montantCalcule = 100;
                        }
                        if(soin == 400 && montantCalcule > 65){
                            montantCalcule = 65;
                        }
                        if(soin == 700 && montantCalcule > 90){
                            montantCalcule = 90;
                        }
                    }
                } // end if B
  
                String montantArembourser = (format.format(montantCalcule)) + "$";
                        
                reclamation.put("soin", soin);
                reclamation.put("date", date);
                reclamation.put("montant", montantArembourser);
                reclamArray.add(reclamation);
            }//end for
            
            JSONObject infoContrat = new JSONObject();
            infoContrat.put("client", client);
            infoContrat.put("mois", mois);
            infoContrat.put("reclamation", reclamArray);
             
            try {           
                FileOutput.write(infoContrat.toString(1));
                FileOutput.flush();
                FileOutput.close();
            }catch(IOException ex){
                System.out.println(ex.getMessage());
            }
            
        }else{
            JSONObject Erreur = new JSONObject();
            Erreur.put("message", "Données invalides");
            try {
                FileOutput.write(Erreur.toString(1));
                FileOutput.flush();
                FileOutput.close();
            }catch(IOException ex){    
                System.out.println(ex.getMessage());
            }
            
        }// end main if

    }// end main
    
    /** Verifier si le numero de client est compose de 6 chiffres et qu'il ne contient aucun caracteres
     * @param client
     * @return 
     */
    public static boolean verifierNumeroClient(String client){
        
        boolean numeroClientValide = false;
        if (client.length() == 6){
            for(int i = 0; i < client.length(); i++){
                numeroClientValide = client.charAt(i) >= '0' && client.charAt(i) <= '9';
                if(!numeroClientValide){
                    i=client.length();
                }
            }
        }
        return numeroClientValide;
     }     
        
    public static boolean verifierContrat(String contrat){
            
        return (contrat.equals("A")|| contrat.equals("B") || contrat.equals("C") || contrat.equals("D"));
        
    }
        
    /** Valider les dates de toutes les reclamations avant de proceder a calculer le montant
    * @param claimArray
    * @param claim
    * @return  */
    public static boolean verifierDate(JSONArray claimArray, JSONObject claim){
        
        boolean dateEstValide = true;
        for (int i =0; i < claimArray.size();i ++){
            if (!claimArray.getJSONObject(i).getString("date").
                    substring(0, 7).equals(claim.getString("mois"))){
                dateEstValide = false;
                i = claimArray.size();
            }
        }
        return dateEstValide;
    }    
        
}