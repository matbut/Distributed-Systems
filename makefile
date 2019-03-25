all:
	
	cp -r ./$(name) ./Buta_Mateusz_$(num)
	tar cvzf Buta_Mateusz_$(num).tar.gz Buta_Mateusz_$(num)
	rm -r Buta_Mateusz_$(num)
