using Microsoft.EntityFrameworkCore;
using RatingCommentsService.Data;
using RatingCommentsService.Models;
using RatingCommentsService.Models.Dto;
using System.Text.Json;

var builder = WebApplication.CreateBuilder(args);

// ----------------------------------------------------------------
// Connection string
// ----------------------------------------------------------------
var conn =
    builder.Configuration.GetConnectionString("Postgres")
    ?? builder.Configuration["ConnectionStrings:Postgres"]
    ?? Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
    ?? "Host=localhost;Port=5432;Database=dogs;Username=postgres;Password=postgres";

Console.WriteLine($"Using connection string: {conn.Substring(0, Math.Min(50, conn.Length))}...");

// ----------------------------------------------------------------
// EF Core + PostgreSQL
// ----------------------------------------------------------------
builder.Services.AddDbContext<AppDb>(opt =>
{
    opt.UseNpgsql(conn);
});

// JSON
builder.Services.AddControllers().AddJsonOptions(opt => 
{
    opt.JsonSerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    opt.JsonSerializerOptions.WriteIndented = true;
});

// Swagger
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// CORS
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAll", policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

// Simple Health Checks (без дополнительных пакетов)
builder.Services.AddHealthChecks();

var app = builder.Build();

// ----------------------------------------------------------------
// Auto migrations OR EnsureCreated
// ----------------------------------------------------------------
using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDb>();
    
    try
    {
        Console.WriteLine("Creating database...");
        db.Database.EnsureCreated();
        Console.WriteLine("Database created successfully.");
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Database error: {ex.Message}");
    }
}

// ----------------------------------------------------------------
// Middleware pipeline
// ----------------------------------------------------------------
if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAll");

// Health check endpoint
app.MapGet("/health", () => Results.Ok(new { status = "OK", timestamp = DateTime.UtcNow }));

// ----------------------------------------------------------------
// API endpoints
// ----------------------------------------------------------------

// POST /ratings/{dogId}
app.MapPost("/ratings/{dogId:long}", async (long dogId, CreateRatingDto dto, AppDb db) =>
{
    if (dto.Value < 1 || dto.Value > 5) 
        return Results.BadRequest(new { error = "Value must be between 1 and 5" });

    var rating = new DogRating 
    { 
        DogId = dogId, 
        Value = dto.Value,
        CreatedAt = DateTime.UtcNow
    };
    
    db.Ratings.Add(rating);
    await db.SaveChangesAsync();

    return Results.Created($"/ratings/{rating.Id}", rating);
});

// GET /ratings/{dogId}
app.MapGet("/ratings/{dogId:long}", async (long dogId, AppDb db) =>
{
    var ratings = await db.Ratings
        .Where(x => x.DogId == dogId)
        .OrderByDescending(x => x.CreatedAt)
        .ToListAsync();

    return Results.Ok(ratings);
});

// GET /ratings/{dogId}/avg
app.MapGet("/ratings/{dogId:long}/avg", async (long dogId, AppDb db) =>
{
    var average = await db.Ratings
        .Where(x => x.DogId == dogId)
        .AverageAsync(x => (double?)x.Value);

    return Results.Ok(new 
    { 
        dogId, 
        average = average ?? 0,
        totalRatings = await db.Ratings.CountAsync(x => x.DogId == dogId)
    });
});

// POST /comments/{dogId}
app.MapPost("/comments/{dogId:long}", async (long dogId, CreateCommentDto dto, AppDb db) =>
{
    if (string.IsNullOrWhiteSpace(dto.Text))
        return Results.BadRequest(new { error = "Comment text is required" });

    var comment = new DogComment 
    { 
        DogId = dogId, 
        Text = dto.Text.Trim(),
        CreatedAt = DateTime.UtcNow
    };
    
    db.Comments.Add(comment);
    await db.SaveChangesAsync();

    return Results.Created($"/comments/{comment.Id}", comment);
});

// GET /comments/{dogId}
app.MapGet("/comments/{dogId:long}", async (long dogId, AppDb db) =>
{
    var comments = await db.Comments
        .Where(x => x.DogId == dogId)
        .OrderByDescending(x => x.CreatedAt)
        .ToListAsync();

    return Results.Ok(comments);
});

// GET /status - simple status endpoint
app.MapGet("/status", () => Results.Ok(new { status = "OK", timestamp = DateTime.UtcNow }));

// Handle OPTIONS for CORS
app.MapMethods("/{*path}", new[] { "OPTIONS" }, () => Results.Ok());

// ----------------------------------------------------------------
// Start the app
// ----------------------------------------------------------------
Console.WriteLine($"Starting Dog Rating Service on port: {Environment.GetEnvironmentVariable("ASPNETCORE_URLS") ?? "8081"}");
app.Run();
