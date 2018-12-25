%{ 
Function controlling the process of evolutionary fitting
params:
    fun - fit function
    xRange - functions domain
    nPop - size of population
    crossChance - chance of crossing, value between [0,1]
    mutChance - chance of mutation, value between [0,1]
    iterCount - max amount of iterations

returns:
    avgFit - average fitness per iteration
    maxFit - maximal fitness per population
    minFit - minimal fitness per population
    approxPoints - x values of population per iteration
    fun - fit function
    xRange - functions domain

%}

function [avgFit, maxFit, minFit, approxPoints, fun, xRange] = pae()

    [fun, xRange, nPop, crossChance, mutChance, iterCount] = setDefaultValues();

    [avgFit, maxFit, minFit] = createFitDataContainers(iterCount);
    approxPoints = createApproxPointsDataContainers(nPop, iterCount);
    
    pop = generatePop(nPop, xRange)
    
    for i = 1:iterCount

        approxPoints = calculateAndAppendPointsToContainer(approxPoints, pop, i, fun);
        fitResults = checkFit(approxPoints(i, :, 2));
        [avgFit, maxFit, minFit] = generateStatsAndAppendToContainers(approxPoints(i, :, 2), avgFit, maxFit, minFit, i);

        pop = rouletteReproduce(pop, fitResults);
        pop = simpleCrossing(pop, crossChance, xRange);
        pop = mutatePop(pop, mutChance, xRange);
    end

    if (i > iterCount)
       disp('Maximal amount of iterations reached') 
    end
    
end

function [fun, xRange, nPop, crossChance, mutChance, iterCount] = setDefaultValues()

    fun = @(x) -0.4.*(x.^2) + x + 10 ; %        0.05 .* (x .^ 2) .* sin(x) - 0.5 .* x     
    xRange = [-4, 24];
    nPop = 10;
    crossChance = 0.4;
    mutChance = 0.01;
    iterCount = 20;
    
end

function [avgFit, maxFit, minFit] = createFitDataContainers(iterCount)
    avgFit = zeros(iterCount, 0);
    maxFit = zeros(iterCount, 0);
    minFit = zeros(iterCount, 0);
end

function approxPoints = createApproxPointsDataContainers(nPop, iterCount)
    approxPoints = zeros(iterCount, nPop, 2);
end

% vector of size nPop with random integer values from set xRange
function pop = generatePop(nPop, xRange)
    pop = (rand(1, nPop)) * (xRange(2) - xRange(1)) + xRange(1);
    pop = round(pop);
end

function approxPoints = calculateAndAppendPointsToContainer(approxPoints, pop, i, fun)
    approxPoints(i, :) = [pop, fun(pop)];
end

function fitResults = checkFit(popYValues)
    popYValues = popYValues - min(popYValues);
    compareValue = sum(popYValues);

    % if the population is homogenous then the compareValue is zero
    % to not divide by zero
    % we set all fitResults to one and replicate the whole population from the first representant
    if(compareValue == 0)
        fitResults = ones(1, size(popYValues, 2));
        return;
    end


    fitResults = popYValues / compareValue;
end

function [avgFit, maxFit, minFit, pScore] = generateStatsAndAppendToContainers(fitResults, avgFit, maxFit, minFit, i)
    avgFit(i) = mean(fitResults);
    maxFit(i) = max(fitResults);
    minFit(i) = min(fitResults);
end

function pop = rouletteReproduce(pop, fitResults)
    pop = distibutionRoulette(pop, fitResults);
end

function pop = distibutionRoulette(pop, fitResults)
    newPop = zeros(1, size(pop, 2));

    for i = 1:size(pop, 2)
        draw = rand();
        for j = 1:size(pop, 2)
            draw = draw - fitResults(j);
            if(draw <= 0)
                newPop(i) = pop(j);
                break;
            end
        end
    end

    pop = newPop;
end

function pop = simpleCrossing(pop, crossChance, xRange)
    [popBit, minValue] = removeNegativeAndTransferToBit(pop);
    
    randomIndexes = getRandomIndexes(size(popBit, 1));
    sizeRandomIndexes = size(randomIndexes,2);
    
    for i = 1:floor(sizeRandomIndexes/2)
        guess = rand();
        if(guess <= crossChance)
            popBit = crossPair(popBit, randomIndexes(i), randomIndexes(sizeRandomIndexes-i+1), xRange, minValue); 
        end
    end
    
    pop = transferToDecimalAndRecoverNegative(popBit, minValue);

end

% we remove the negative values 
% for the crossing process and put them back after it
function [popBit, minValue] = removeNegativeAndTransferToBit(pop)
    minValue = min(pop);
    pop = pop - minValue;
    popBit = de2bi(pop, 16);
end

function pop = transferToDecimalAndRecoverNegative(popBit, minValue)
    % transpose is needed because bi2de returns column vector

    pop = bi2de(popBit)';
    pop = pop + minValue;
end

function randomIndexes = getRandomIndexes(sizePop)
    randomIndexes = randperm(sizePop);
end

function popBit = crossPair(popBit, firstIndex, secondIndex, xRange, minValue)
    kIndex = getKIndex(size(popBit, 2));
    popBit = swapBits(popBit, firstIndex, secondIndex, kIndex);

    if(checkIfOutOfRange(popBit, firstIndex, secondIndex, xRange, minValue))
        popBit = swapBits(popBit, firstIndex, secondIndex, kIndex);
    end
end

function kIndex = getKIndex(nPop)
    kIndex = round(rand()*(nPop - 2) + 1);
end

function popBit = swapBits(popBit, firstIndex, secondIndex, kIndex)
    tmp = popBit(firstIndex, 1:kIndex);
    popBit(firstIndex, 1:kIndex) = popBit(secondIndex, 1:kIndex);
    popBit(secondIndex, 1:kIndex) = tmp;
end

function test = checkIfOutOfRange(popBit, firstIndex, secondIndex, xRange, minValue)
    firstX = transferToDecimalAndRecoverNegative(popBit(firstIndex, :), minValue);
    secondX = transferToDecimalAndRecoverNegative(popBit(secondIndex, :), minValue);

    if(outOfXRange(firstX, xRange) || outOfXRange(secondX, xRange))
        test = 1;
        return
    end

    test = 0;
end

function test = outOfXRange(value, xRange)
     test = value < xRange(1) || value > xRange(2);
end

function pop = mutatePop(pop, mutChance, xRange)
    if(mutChance == 0)
        return
    end

    [popBit, minValue] = removeNegativeAndTransferToBit(pop);

    for i = 1:(size(popBit, 1) * size(popBit, 2))
        guess = rand();
        if(guess < mutChance)   

            popBit = flipBit(popBit, i);

            if(mutationCheckIfOutOfRange(popBit, i, xRange, minValue))
                popBit = flipBit(popBit, i);
            end
        end
    end

    pop = transferToDecimalAndRecoverNegative(popBit, minValue);
end

function popBit = flipBit(popBit, index)
    if(popBit(index) == 1)
        popBit(index) = 0;
    else
        popBit(index) = 1;
    end
end

function test = mutationCheckIfOutOfRange(popBit, bitIndex, xRange, minValue)
    rowIndex = calculateRow(bitIndex, size(popBit, 1));
    firstX = transferToDecimalAndRecoverNegative(popBit(rowIndex, :), minValue);

    if(outOfXRange(firstX, xRange))
        test = 1;
        return
    end

    test = 0;
end

function rowIndex = calculateRow(bitIndex, n)
    rowIndex = mod(bitIndex, n);
    if(rowIndex == 0)
        rowIndex = n;
    end
end
