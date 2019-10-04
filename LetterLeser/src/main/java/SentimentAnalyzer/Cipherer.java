import javax.crypto.Cipher;

/**
 * @author Phoenix Boisnier
 * This class is used to encipher text.
 */
public class Cipherer {

    private static String[][] vigenere = {{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"},
                                   {"b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a"},
                                   {"c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b"},
                                   {"d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c"},
                                   {"e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d"},
                                   {"f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e"},
                                   {"g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f"},
                                   {"h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g"},
                                   {"i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h"},
                                   {"j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i"},
                                   {"k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j"},
                                   {"l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k"},
                                   {"m","n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l"},
                                   {"n","o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m"},
                                   {"o","p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n"},
                                   {"p","q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o"},
                                   {"q","r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p"},
                                   {"r","s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q"},
                                   {"s","t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r"},
                                   {"t","u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s"},
                                   {"u","v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t"},
                                   {"v","w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u"},
                                   {"w","x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v"},
                                   {"x","y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w"},
                                   {"y","z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x"},
                                   {"z","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y"}};
    private static String[] caesar = vigenere[0];

    /**
     * This method enciphers the given text.
     * @param type 0 is Caesar, 1 is Vigenere.
     * @param param Caesar requires input string as param[0] and integer shift value as param[1].
     *              Vigenere requires input string as param[0] and keyword string as param[1].
     * @return This is the encoded string.
     */
    public static String encipher(int type, String[] param) throws IndexOutOfBoundsException {
        String result = "";
        switch(type) {
            //This is a caesar shift.
            case 0: {
                //This makes sure that the second parameter is an integer.
                try {
                    int shift = Integer.parseInt(param[1]);
                    //This does the actual encryption.
                    for(char c : param[0].toCharArray()) {
                        //Lower case letters.
                        if(c >= 97 && c <= 122) {
                            int org = Cipherer.getIndex(Cipherer.caesar, c+"");
                            int dex = (org+shift) % Cipherer.caesar.length;
                            result = result + Cipherer.caesar[dex];
                        }
                        //Upper case letters.
                        else if(c >= 65 && c <= 90) {
                            char d = (char)(c+32);
                            int org = Cipherer.getIndex(Cipherer.caesar, d+"");
                            int dex = (org+shift) % Cipherer.caesar.length;
                            char e = (char)(Cipherer.caesar[dex].toCharArray()[0]-32);
                            result = result + e;
                        }
                        //Non-letters.
                        else {
                            result = result + c;
                        }
                    }
                    break;
                }
                catch(NumberFormatException e) {
                    System.out.println("Caesar shift requires param[1] to be of type int.");
                    e.printStackTrace();
                    break;
                }
            }
            //This is the Vignere Cipher.
            default: {
                int kInd = 0;
                //For each letter in the string,
                for(char c : param[0].toCharArray()) {
                    //Lower case letters.
                    if(c >= 97 && c <= 122){
                        int row = Cipherer.getIndex(Cipherer.caesar, param[1].substring(kInd, kInd+1));
                        String[] shift = vigenere[row];
                        int col = Cipherer.getIndex(caesar, c+"");
                        result = result + shift[col];
                        kInd = (kInd+1) % param[1].length();
                    }
                    //Upper case letters.
                    else if(c >= 65 && c <= 90){
                        char d = (char)(c+32);
                        int row = Cipherer.getIndex(Cipherer.caesar, param[1].substring(kInd, kInd+1));
                        String[] shift = vigenere[row];
                        int col = Cipherer.getIndex(caesar, d+"");
                        char e = (char)(shift[col].toCharArray()[0]-32);
                        result = result + e;
                        kInd = (kInd+1) % param[1].length();
                    }
                    //Non-letters.
                    else {
                        result = result + c;
                        kInd = (kInd+1) % param[1].length();
                    }
                }
                break;
            }
        }
        return result;
    }

    /**
     * Gets the index of the string.
     * @param arr The array.
     * @param s The string.
     * @return The index, else -1.
     */
    private static int getIndex(String[] arr, String s){
        for(int q = 0; q < arr.length; q++){
            if(arr[q].equals(s)){
                return q;
            }
        }
        return -1;
    }

}
