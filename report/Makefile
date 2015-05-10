all:	report.tex references.bib
	pdflatex report.tex
	bibtex report
	pdflatex report.tex
	pdflatex report.tex

clean:
	rm -f *.aux
	rm -f *.pdf
	rm -f *.log
	rm -f *.bcf
	rm -f *.run.xml
	rm -f *.bbl
	rm -f *.blg
