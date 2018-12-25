function graph_pae(fun, xRange, approxPoints)
    X = xRange(1):0.1:xRange(2);
    
    h = figure;
    axis tight manual
    filename = 'graph_pae.gif';
    plot(X, fun(X));

    hold on
    for i = 1:size(approxPoints,1)
        plt = plot(approxPoints(i,:,1), approxPoints(i, :, 2), '*');

        saveFrame(h, filename, i);

        waitforbuttonpress;
        set(plt, 'Visible', 'off');
    end

    hold off;
end

function saveFrame(h, filename, i)
    frame =  getframe(h);
    im = frame2im(frame);
    [imind, cm] = rgb2ind(im, 256);

	if i == 1 
	  imwrite(imind,cm,filename,'gif', 'Loopcount',inf); 
	else 
	  imwrite(imind,cm,filename,'gif','WriteMode','append'); 
	end 
end