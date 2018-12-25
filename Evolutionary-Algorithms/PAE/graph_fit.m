function graph_fit(avgFit, minFit, maxFit)
	
	subplot(3, 1, 1)
	plot(avgFit)
	title('Average fitness')

	subplot(3, 1, 2)
	plot(minFit)
	title('Minimal fitness')

	subplot(3, 1, 3)
	plot(maxFit)
	title('Maximal fitness')

	subplot(3, 3, [2 3 5 6 8 9])
	X = xRange(1):0.1:xRange(2);
    plot(X, fun(X));

end