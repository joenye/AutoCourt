start-desktop:
	./install.sh && docker-compose -f docker-compose-desktop.yml up --build

start-pi:
	./install.sh && docker-compose -f docker-compose-pi.yml up --build

