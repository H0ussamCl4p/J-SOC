package jsoc;

/**
 * Point d'entrée de l'application J-SOC.
 *
 * <p>Cette classe est volontairement minimale à ce stade — c'est le <b>Membre 4
 * (CLI)</b> qui en prendra la main pour brancher le menu de connexion, le routage
 * vers les menus analyste/manager et la boucle principale du programme.</p>
 *
 * <p>Membre 1 (architecte) la fournit ici uniquement pour que le projet compile
 * et soit exécutable dès aujourd'hui — un sanity check avant que les autres
 * membres ne commencent leurs commits.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("J-SOC - Java SOC Incident Management Platform");
        System.out.println("Version 0.1.0 - In development");
        // TODO (M4) : implémenter ici le menu de connexion
        //  -> AuthService.login() -> AnalystMenu / ManagerMenu selon le rôle
    }
}
