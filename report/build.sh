#!/bin/bash

texi2pdf report.tex > /dev/null

pandoc report.tex -o ../README.md
