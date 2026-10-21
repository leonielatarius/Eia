SimpleMinim minim ; 
SimpleaudioPlayer audioPlayer; 

minim = new SimpleMinim(); 
audioPlayer = minim.loadMP3File(filename); 

audioPlayer.play(); s