obrob <- function(m){
  
  # usuwamy kolumne X i Name
  m <- m[c(-1,-2)]
  
  # zamieniamy Legendary na boolean
  m$Legendary <- as.integer(as.logical(m$Legendary))
  
  # stosujemy normalizacje min-max na zbiorze
  m <- normalizuj(m, typ="norm", atryb=c(3,4,5,6,7,8,9,10))
  
  m
}