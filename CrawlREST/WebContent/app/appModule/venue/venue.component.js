angular.module('appModule')
.component('venue',{
	templateUrl: 'app/appModule/venue/venue.component.html',
	controller: function(venueService, $routeParams, $location){
		
		var vm = this;
		vm.venue = null;
		vm.copy = null;
		vm.update = null;
		vm.updateAddress = null;
		vm.states = ["AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL","GA","HI","ID"
			,"IL","IN","IA","KS","KY","LA","ME","MT","NE","NV","NH","NJ","NM","NY","NC"
			,"ND","OH","OK","OR","MD","MA","MI","MN","MS","MO","PA","RI","SC","SD","TN"
			,"TX","UT","VT","VA","WA","WV","WI","WY"
		];
		
		loadVenue();
		
		vm.showCreate = function(){
			vm.copy = {};
			vm.copyAddress = {};
			console.log(vm.copy);
		};
		vm.cancelCreate = function(){
			vm.copy = null;
		}
		vm.createVenue = function(){
			console.log(vm.copy);
			venueService.createVenue(vm.copy)
			.then(function(res){
				console.log(res.data);
				var id = res.data.id;
				$location.path("/venue/" + id);
			})
			.catch(function(err){
				console.log(err);
			});
		}
		
		vm.showUpdate = function(){
			vm.update = angular.copy(vm.venue);
			vm.updateAddress = vm.update.address;
			console.log(vm.update);
			console.log(vm.updateAddress);
		};
		vm.updateVenue = function(){
			console.log(vm.update);
			vm.update.address = null;
			venueService.updateVenue(vm.update, $routeParams.vid)
			.then(function(res){
				vm.update = null;
				vm.updateAddress = null;
				loadVenue();
			})
			.catch(function(err){
				console.log(err);
			})
		};
		vm.updateVenueAddress = function(){
			console.log(vm.updateAddress);
			venueService.updateAddress(vm.updateAddress, $routeParams.vid)
			.then(function(res){
				vm.update = null;
				vm.updateAddress = null;
				loadVenue();
			})
			.catch(function(err){
				console.log(err)
			})
		};
		vm.cancelUpdate = function(){
			vm.update = null;
			vm.updateAddress = null;
		}
		
		function loadVenue(){
			venueService.showVenue($routeParams.vid)
			.then(function(res){
				vm.venue = res.data;
			})
			.catch(function(err){
				console.log(err);
			});
		}
	},
	controllerAs: 'vm'
});