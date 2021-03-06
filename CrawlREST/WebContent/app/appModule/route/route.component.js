angular.module('appModule').component('route', {
	templateUrl : "app/appModule/route/route.component.html",
	controller : function(routeService, $routeParams, $cookies, $rootScope, $location) {
		//Variables
		var vm = this;

		vm.route = null;
		vm.routeVenues = null;
		vm.allVenues = null;
		vm.admin = null;

		vm.loadRoute = function() {
			var promise = routeService.showRoute($routeParams.rid);

			promise.then(function(res){
				console.log(res);
				vm.route = res.data;
				vm.loadAllVenuesExcept(vm.route.id);
			}).catch(function(err){
				console.log(err);
			});
		}

		vm.adminCheck = function() {
			var promise = routeService.adminCheck($routeParams.rid, $cookies.get('userId'));
			promise.then(function(res){
				vm.admin = res.data;
			}).catch(function(err){
				console.log(err);
			});

		}

		vm.adminCheck();

		vm.addVenue = function(rid, venue) {
			routeService.addVenue(rid, venue.id)
			.then(function(res){
				vm.resetPage();
			})
		}

		vm.newVenuePage = function(){
			$location.path('#/venue/');
		}
		
		vm.removeVenue = function(rid,vid) {
			routeService.removeVenue(rid,vid)
			.then(function(res){
				vm.resetPage();
			})
		}


		vm.venueUp = function(rid,vid) {
			routeService.moveVenueUp(rid,vid)
			.then(function(res){
				vm.resetPage();
			})
		}
		vm.venueDown = function(rid,vid) {
			routeService.moveVenueDown(rid,vid)
			.then(function(res){
				vm.resetPage();
			})
		}


		vm.loadRouteVenues = function() {
			routeService.indexRouteVenues($routeParams.rid)
			.then(function(res) {
				vm.routeVenues = res.data;
				var cords = CreateCordArray();
				$rootScope.$broadcast('map', {
					center: cords[0],
					markers : cords,
					zoom: 14
				});
			})
		};


		vm.loadAllVenuesExcept = function (rid){
			routeService.indexAllVenues(rid).then(function(res){
				vm.allVenues = res.data;
			})
		};


		vm.loadRoute();
		vm.loadRouteVenues();
		vm.loadAllVenuesExcept();

		vm.loadMembers = function() {
			groupService.indexMembers()
			.then(function(res) {
				vm.members = res.data;
			})
		};

		vm.updateRoute = function(route) {
			routeService.updateRoute(route, $routeParams.rid)
			.then(function(res) {
				vm.loadRoute();
				vm.editRoute = null;
			})
			.catch(function(err) {
				console.log(err);
			})
		};

		vm.resetPage = function() {
			vm.loadRoute();
			vm.loadRouteVenues();
			vm.loadAllVenuesExcept(vm.route.id);
		}
		function CreateCordArray(){
			arr = [];
			vm.routeVenues.forEach(function(val){
				var cord = {
					lng : val.venue.address.longitude,
					lat : val.venue.address.latitude,
					title : val.venue.name	
				};
				arr.push(cord);
			})
			return arr;
		}

	},
	controllerAs: 'vm'
});
