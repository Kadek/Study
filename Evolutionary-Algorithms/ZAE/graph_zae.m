function graph_zae(avgFit, minFit, maxFit, mapSize, citiesCoords, minCostPath, maxCostPath)
	
	plotH = 4;
	plotW = 6;

	figure('units','normalized','outerposition',[0 0 1 1])

	plotAvgFit(plotW, plotH, [19 20 21 22 23 24], avgFit);
	plotMinFit(plotW, plotH, [13 14 15], minFit);
	plotMaxFit(plotW, plotH, [16 17 18], maxFit);

   	map = getMap(mapSize, citiesCoords);

	plotCostPath(plotW, plotH, [1 2 3 7 8 9], 'Best Path', map, citiesCoords, minCostPath)
	plotCostPath(plotW, plotH, [4 5 6 10 11 12], 'Worst Path',map, citiesCoords, maxCostPath)

	saveas(gcf, 'screenshot.png');
end

function plotAvgFit(plotW, plotH, plotSector, avgFit)

	subplot(plotH, plotW, plotSector)
	plot(avgFit)
	title('Average fitness')

end

function plotMinFit(plotW, plotH, plotSector, minFit)

	subplot(plotH, plotW, plotSector)
	plot(minFit)
	title('Minimal fitness')

end

function plotMaxFit(plotW, plotH, plotSector, maxFit)

	subplot(plotH, plotW, plotSector)
	plot(maxFit)
	title('Maximal fitness')

end

function map = getMap(mapSize, citiesCoords)

   	map = ones(mapSize);
   	citiesIndices = (citiesCoords(1, :)-1) .* mapSize(1) + citiesCoords(2, :)
   	map(citiesIndices) = 0

end

function plotCostPath(plotW, plotH, plotSector, titleText, map, citiesCoords, costPath)

	subplot(plotH, plotW, plotSector);
	hold on
	imshow(map, 'InitialMagnification', 'fit');
	title(titleText);

	for lineIndex = 1:(size(citiesCoords, 2) - 1)

		cityIndex1 = costPath(lineIndex);
		cityIndex2 = costPath(lineIndex+1);

		x = [citiesCoords(1, cityIndex1); citiesCoords(1, cityIndex2)];
		y = [citiesCoords(2, cityIndex1); citiesCoords(2, cityIndex2)];

		line(x, y);

	end

	x = [citiesCoords(1, costPath(1)); citiesCoords(1, costPath(size(citiesCoords, 2)))];
	y = [citiesCoords(2, costPath(1)); citiesCoords(2, costPath(size(citiesCoords, 2)))];

	line(x, y);

	hold off

end