# Build stage
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY . .
RUN dotnet restore
RUN dotnet publish -c Release -o /app/publish

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
EXPOSE 8081
ENV ASPNETCORE_URLS=http://*:8081
COPY --from=build /app/publish .
ENTRYPOINT ["dotnet", "DogRatingService.dll"]