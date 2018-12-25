%{ 
Function controlling the process of evolutionary fitting
params:
    mapSize - the size of the map used for displaying
    citiesCords - coordinates of the cities
    nPop - size of population
    crossChance - chance of crossing, value between [0,1]
    mutChance - chance of mutation, value between [0,1]
    iterCount - max amount of iterations

returns:
    avgFit - average fitness per iteration
    maxFit - maximal fitness per population
    minFit - minimal fitness per population
    mapSize - the size of the map used for displaying
    citiesCords - coordinates of the cities
    minCostPath - path of the best fitness/smallest cost
    maxCostPath - path of the worst fitness/biggest cost
     

%}

function [avgFit, maxFit, minFit, mapSize, citiesCoords, minCostPath, maxCostPath] = zae()

    [mapSize, citiesCoords, nPop, crossChance, mutChance, iterCount] = setDefaultValues();
    nCities = size(citiesCoords, 2);

    [avgFit, maxFit, minFit] = createFitDataContainers(iterCount);
    [minCostPath, maxCostPath] = createCostPathDataContainers(nCities);
    
    pop = generatePop(nPop, nCities);
    
    globalMinimum = 0;
    bestFit = 0;
    worstFit = 0;

    for i = 1:iterCount

        [fitResults, globalMinimum] = checkFit(pop, citiesCoords, globalMinimum);
        [avgFit, maxFit, minFit] = generateStatsAndAppendToContainers(fitResults, avgFit, maxFit, minFit, i);

        [bestFit, worstFit] = updateEdgeFit(bestFit, worstFit, fitResults);
        [minCostPath, maxCostPath] = updateCostPaths(minCostPath, maxCostPath, bestFit, worstFit, fitResults, pop);

        pop = rouletteReproduce(pop, fitResults);
        pop = PMXCrossing(pop, crossChance);
        pop = mutatePop(pop, mutChance);

    end
    if (i > iterCount)
       disp('Maximal amount of iterations reached') 
    end
    
end

function [mapSize, citiesCoords, nPop, crossChance, mutChance, iterCount] = setDefaultValues()

    mapSize = [10, 10];
    citiesCoords = [2 9 6 1 3 5 4 10 7 5; 3 9 7 9 8 8 4 6 5 8];
    nPop = 15;
    crossChance = 1;
    mutChance = 0.01;
    iterCount = 1000;
    
end

function [avgFit, maxFit, minFit] = createFitDataContainers(iterCount)

    avgFit = zeros(iterCount, 0);
    maxFit = zeros(iterCount, 0);
    minFit = zeros(iterCount, 0);

end

function [minCostPath, maxCostPath] = createCostPathDataContainers(nCities)

    minCostPath = zeros(nCities, 1);
    maxCostPath = zeros(nCities, 1);

end

% vector of size Pop with permutation of possible cities
function pop = generatePop(nPop, nCities)

    pop = zeros(nPop, nCities);
    for i = 1:nPop
        pop(i, :) = randperm(nCities);
    end

end

function [fitResults, globalMinimum] = checkFit(pop, citiesCoords, globalMinimum)

    nPop = size(pop, 1);
    fitResults = zeros(nPop, 1);

    for i = 1:nPop
        fitResults(i) = calculateDistance(pop(i, :), citiesCoords);
    end

    fitResults = -fitResults;
    globalMinimum = updateGlobalMinimum(fitResults, globalMinimum);
    fitResults = fitResults - globalMinimum;

end

function distance = calculateDistance(pathFromPop, citiesCoords)

    distance = 0;
    for cityIndex = 1:(size(pathFromPop, 2) - 1)
        distance = distance + euclideanDistance(citiesCoords(:, pathFromPop(cityIndex)), citiesCoords(:, pathFromPop(cityIndex + 1)));
    end
    distance = distance + euclideanDistance(citiesCoords(:, pathFromPop(1)), citiesCoords(:, pathFromPop(cityIndex + 1)));

end

function distance = euclideanDistance(p1, p2)

    distance = sqrt(sum((p1 - p2) .^ 2));

end

function globalMinimum = updateGlobalMinimum(fitResults, globalMinimum)

    if(min(fitResults) < globalMinimum)
        globalMinimum = min(fitResults);
    end    

end

function [avgFit, maxFit, minFit, pScore] = generateStatsAndAppendToContainers(fitResults, avgFit, maxFit, minFit, i)

    avgFit(i) = mean(fitResults);
    maxFit(i) = max(fitResults);
    minFit(i) = min(fitResults);

end
    
function [bestFit, worstFit] = updateEdgeFit(bestFit, worstFit, fitResults)

    if(max(fitResults) > bestFit)

        bestFit = max(fitResults);

    end

    if(min(fitResults) < worstFit)

        worstFit = min(fitResults);

    end
end

function [minCostPath, maxCostPath] = updateCostPaths(minCostPath, maxCostPath, bestFit, worstFit, fitResults, pop)

    % biggest fit is the lowest cost
    if(max(fitResults) == bestFit)

        minCostIndex = find(abs(fitResults-max(fitResults))<1e-3);
        minCostPath = pop(minCostIndex(1), :);

    end

    if(min(fitResults) == worstFit)

        maxCostIndex = find(abs(fitResults-min(fitResults))<1e-3);
        maxCostPath = pop(maxCostIndex(1), :);

    end

end

function pop = rouletteReproduce(pop, fitResults)

    percentageFitResults = calculatePercentageFitResults(fitResults);
    pop = distibutionRoulette(pop, percentageFitResults);

end

function percentageFitResults = calculatePercentageFitResults(fitResults)

    if(~any(fitResults))
        percentageFitResults = fitResults;
        percentageFitResults(1) = 1;
        return;
    end    
    percentageFitResults = fitResults ./ sum(fitResults);

end

function pop = distibutionRoulette(pop, percentageFitResults)
    newPop = zeros(size(pop));

    for i = 1:size(pop, 1)
        draw = rand();
        for j = 1:size(pop, 1)
            draw = draw - percentageFitResults(j);
            if(draw <= 0)
                newPop(i, :) = pop(j, :);
                break;
            end
        end
    end

    pop = newPop;
end

function pop = PMXCrossing(pop, crossChance)
    
    randomIndexes = getRandomIndexes(size(pop, 1));
    sizeRandomIndexes = size(randomIndexes,1);
    
    for i = 1:floor(sizeRandomIndexes/2)
        guess = rand();
        if(guess <= crossChance)
            child1 = crossPair(pop(randomIndexes(i), :), pop(randomIndexes(sizeRandomIndexes-i+1), :));
            child2 = crossPair(pop(randomIndexes(sizeRandomIndexes-i+1), :), pop(randomIndexes(i), :));

            pop(randomIndexes(i), :) = child1;
            pop(randomIndexes(sizeRandomIndexes-i+1), :) = child2;
        end
    end
    

end

function randomIndexes = getRandomIndexes(sizePop)

    randomIndexes = randperm(sizePop)';

end

function child = crossPair(parent1, parent2)

    child = zeros(size(parent1));
    swath = getRandomSwath(size(parent1, 2));
    child(swath(1):swath(2)) = parent1(swath(1):swath(2));

    child = swathMagic(parent1, parent2, child, swath);

    child = fillBlanksWithParent2(child, parent2);
end

function swath = getRandomSwath(n)

    swath = [randi(n), randi(n)];
    swath = sort(swath);

end

function child = swathMagic(parent1, parent2, child, swath)

    for i = swath(1):swath(2)
        firstValue = parent2(i);

        if(~ismember(firstValue, child))

            while(1)
                flag = 1;

                    firstValueIndex = find(parent2 == firstValue);
                    valueAtTheSameIndex = parent1(firstValueIndex);

                    indexOfTheRepeatValue = find(parent2 == valueAtTheSameIndex);

                    if(indexOfTheRepeatValue >= swath(1) && indexOfTheRepeatValue <= swath(2))
                        firstValue = parent2(indexOfTheRepeatValue);
                        flag = 0;
                    else
                        child(indexOfTheRepeatValue) = parent2(i);
                    end

                if(flag)
                    break;
                end

            end

        end
    end

end

function child = fillBlanksWithParent2(child, parent2)

    child = child + ((child == 0) .* parent2);

end

function pop = mutatePop(pop, mutChance)

    if(mutChance == 0)
        return
    end

    for i = 1:size(pop, 1)
        for j = 1:size(pop, 2)
            guess = rand();
            if(guess < mutChance)   

                pop = swapRandom(pop, i, j);

            end
        end
    end

end

function pop = swapRandom(pop, row, col)

    col2 = randi(size(pop, 2));
    while(col2 == col)
        col2 = randi(size(pop, 2));
    end

    tmp = pop(row, col);
    pop(row, col) = pop(row, col2);
    pop(row, col2) = tmp;

end