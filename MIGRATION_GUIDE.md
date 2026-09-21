# Guide de Déploiement Firebase & Migration des Abonnés

Ce guide résume les étapes simples pour mettre en service le backend sécurisé Firebase de **Loto Niger Analytics** et réémettre les codes des abonnés actuels.

---

## 1. Configuration Firebase (En 5 étapes rapides)

### Étape 1 : Créer ou associer le projet Firebase
1. Rendez-vous sur la [Console Firebase](https://console.firebase.google.com/).
2. Sélectionnez ou créez le projet Google Cloud correspondant à votre application.
3. Téléchargez le fichier `google-services.json` de votre application Android et placez-le dans le dossier `app/` du projet.

### Étape 2 : Activer les services d'authentification
Dans le menu **Authentication** > **Sign-in method** :
- Activez **Connexion anonyme** (utilisée automatiquement et de manière transparente par l'application pour sécuriser les sessions et tokens).
- Activez **Adresse e-mail / Mot de passe** (utilisée uniquement par le propriétaire pour l'écran d'administration).
- Créez votre compte administrateur propriétaire avec votre adresse email `bachiroudjibomali@gmail.com`.

### Étape 3 : Déployer les règles Firestore et les Cloud Functions
Depuis votre terminal dans le projet :
```bash
# Se connecter à Firebase
firebase login

# Déployer les règles de sécurité Firestore
firebase deploy --only firestore:rules

# Déployer les fonctions backend
cd functions
npm install
firebase deploy --only functions
```

### Étape 4 : Attribuer le droit Administrateur Propriétaire
Exécutez le script d'initialisation pour votre compte administrateur :
```bash
node functions/setup_admin.js bachiroudjibomali@gmail.com
```

### Étape 5 : Activer Firebase App Check (Optionnel mais recommandé)
Dans la console Firebase, allez dans **App Check** et enregistrez votre application Android avec le fournisseur Play Integrity / ReCaptcha.

---

## 2. Guide de Migration des Abonnés Actuels

### Pourquoi les anciens codes cessent de fonctionner ?
Auparavant, les codes étaient vérifiés côté client dans l'application avec de simples préfixes (`ESS-`, `PRO-`) ou listes hachées statiques. Ces codes ne vérifiaient ni le nombre d'appareils, ni la véritable date d'expiration serveur, et étaient vulnérables à la modification locale.

Désormais, **seuls les codes signés par le serveur et stockés dans la collection Firestore chiffrée** sont acceptés.

### Procédure pour réémettre les codes des abonnés actifs :
1. Ouvrez l'application **Loto Niger Analytics**.
2. Allez dans l'onglet **Plus** > **Espace Propriétaire (Administration)**.
3. Connectez-vous avec votre adresse propriétaire (`bachiroudjibomali@gmail.com`) et votre mot de passe.
4. Dans la section **Créer un code** :
   - Choisissez la formule de l'abonné (**Premium** ou **Premium+**).
   - Saisissez le nom de l'abonné (ex: *"Moussa Abdou - Airtel Money"*).
   - Cliquez sur **Générer le code**.
5. Cliquez sur le bouton vert **Envoyer par WhatsApp** : l'application ouvre directement WhatsApp avec un message pré-rempli contenant son nouveau code d'accès sécurisé valable 30 jours pour ses 2 appareils.
6. L'abonné colle simplement ce code dans son écran **Premium** pour réactiver son accès instantanément.
