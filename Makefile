writeup.pdf: writeup.tex
	pdflatex writeup; biber writeup; pdflatex writeup; pdflatex writeup

clean:
	rm -f *.aux
	rm -f *.pdf
	rm -f *.log
	rm -f *.bcf
	rm -f *.run.xml
	rm -f *bbl
	rm -f *.blg
